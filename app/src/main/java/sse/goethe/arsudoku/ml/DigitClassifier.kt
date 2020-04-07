/**
 * @author: David Machajewski
 * date: 26.01.2020
 *
 */
package sse.goethe.arsudoku.ml
import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks.call
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.Interpreter
import sse.goethe.arsudoku.ml.Keys.MODEL_PATH
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.io.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Callable

/**
 * This class classifies the digits
 * inside the cropped squares from
 * the Sudoku.
 *
 * @param context
 * @author David Machajewski
 * */
class DigitClassifier(private val context: Context) {
    private  var interpreter: Interpreter? = null
    var isInitialized = false
    private var inputImageWidth: Int = 0 // will be inferred from TF Lite model
    private var inputImageHeight: Int = 0 // will be inferred from TF Lite model
    private var modelInputSize: Int = 0 // will be inferred from TF Lite model

    /** Executor for inference bc this process is not thread save */
    private val executorService: ExecutorService = Executors.newCachedThreadPool()

    /**
     * The initializeInterpreter() function has to be called
     * before you launch the interpreter.run().
     *
     * Do not forget to close() the Interpreter
     * */
    fun initializeInterpreter() {
        Log.d("DigitClassifier", "initializeInterpreter()")
        val assetManager = context.assets // load model
        val model = loadModelFile(assetManager)
        val options = Interpreter.Options()
        //options.setNumThreads(2) // NUMBER OF THREADS USED ...
        options.setUseNNAPI(true) // NNAPI provides acceleration for devices GPU; DSP, NPU
        val interpreter = Interpreter(model, options)

        // Read input shape from model file
        val inputShape = interpreter.getInputTensor(0).shape()
        inputImageWidth = inputShape[1]
        Log.d(TAG, "inputShape 1" + inputShape[1])
        inputImageHeight = inputShape[2]
        Log.d(TAG, "inputShape 1" + inputShape[2])
        modelInputSize = FLOAT_TYPE_SIZE * inputImageWidth * inputImageHeight * PIXEL_SIZE
        Log.d(TAG, "The model input size is: " + modelInputSize)
        this.interpreter = interpreter
        isInitialized = true
        Log.d(TAG, " >> Initialized TFLite interpreter! << ")
    }

    /**
     * The loadModelFile() function...
     * Input:
     * Output:
     * */
    @Throws(IOException::class)
    fun loadModelFile(assetManager: AssetManager): ByteBuffer {
        /*
        * The main practical use for a file descriptor is to create
        * a FileInputStream or FileOutputStream to contain it
        */
        val fileDescriptor = assetManager.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * The convertBitmapToBytebuffer() function takes a Bitmap
     * of the sudoku and convert it to a ByteBuffer.
     * The ByteBuffer is needed for the TensorFlow Classifier
     *
     * Input:
     * Output:
     *
     * */
    private fun convertBitmapToBytebuffer(bitmap: Bitmap): ByteBuffer {

        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputImageWidth * inputImageHeight)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in pixels) {
            val r = (pixelValue shr 16 and 0xFF)
            val g = (pixelValue shr 8 and 0xFF)
            val b = (pixelValue and 0xFF)

            // Log.d("DigitClassifier:", "r: $r g: $g b:$b")
            // convert RGB to grayscale and normalize it
            var normalizedPixelValue = (r + g + b) / 3.0f / 255.0f
            if (normalizedPixelValue > 0.5) {
                normalizedPixelValue = 1.0F
            } else {
                normalizedPixelValue = 0.0F
            }
            //Log.d(TAG, "norm pxval: $normalizedPixelValue")
            byteBuffer.putFloat(normalizedPixelValue)
        }
        return byteBuffer
    }

    /**
     * The classify() function interprets the Sudoku Bitmap and
     * ...
     *
     * Input:
     * Output:
     *
     * */

    @RequiresApi(Build.VERSION_CODES.Q)
    fun classify(bitmap: Bitmap): Int {
        if (!isInitialized) {
            throw IllegalStateException(" TF Lite Interpreter is not initialized yet. ")
        }

        var startTime: Long
        var elapsedTime: Long

        startTime = System.nanoTime()
        val resizedImage = Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true) // evtl. rausnehmen

        // check if field is empty, if yes do not classify this image
        var isEmpty = isEmptyField(resizedImage) // evtl. rausnehmen oder verwenden

        val byteBuffer = convertBitmapToBytebuffer(resizedImage)
        val result = Array(1) { FloatArray(OUTPUT_CLASSES_COUNT) }
        elapsedTime = (System.nanoTime() - startTime) / 1000000
        Log.d(TAG, "Preprocessing time = " + elapsedTime + "ms")

        // measure inference time
        startTime = System.nanoTime()

        interpreter?.run(byteBuffer, result) // evtl. alle auf einmal verarbeiten
        /*
        Log.d(TAG, "result array: res 1: " + result[0][0] + " res 2: " + result[0][1] + "res 3: " + result[0][2]
                + " res 2: " + result[0][3] + " res 2: " + result[0][4] + " res 2: " + result[0][5] + " res 2: " + result[0][6]
                + " res 2: " + result[0][7] + " res 2: " + result[0][8] + " res 2: " + result[0][9] + " res 2: " + result[0][10]
                + " res 2: " + result[0][11] + " res 2: " + result[0][12] + " res 2: " + result[0][13] + " res 2: " + result[0][14]
                + " res 2: " + result[0][15] + " res 2: " + result[0][16] + " res 2: " + result[0][17] + " res 2: " + result[0][18]
                + " res 2: " + result[0][19] )
        */
        elapsedTime = (System.nanoTime() - startTime) / 1000000
        Log.d(TAG, "Inference time = " + elapsedTime + "ms")

        return getOutputInt(result[0])
    }

    /**
     * The classifier has some issues to classify
     * the empty squares.
     *
     * */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun isEmptyField(bitmap: Bitmap): Boolean {
        var px = 0
        var lbound = 0.9

        //for (row in 0..27) {
        //    for (col in 0..27) {
        //       px = Color.red(bitmap.getPixel(row, col))
        //        Log.d(TAG, "Pixel: $px")
        //    }
        //}

        return false
    }

    /**
     *
     * */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun classifyAsynchronous(bitmap: Bitmap): Task<Int> {
        return call(executorService, Callable<Int> {classify(bitmap)} )
    }

    /**
     * The getOutputString() does the same as getOutputInt()
     * but returns a String.
     *
     * Input:
     * Output:
     * */
    private fun getOutputString(output: FloatArray): String {
        val maxIndex = output.indices.maxBy { output[it] } ?: -1
        return return "Prediction Result: %d\nConfidence: %2f".format(maxIndex, output[maxIndex])
    }

    /**
     * The getOutputInt() function takes the inference result
     * of the TFLite Model and returns the biggest value.
     *
     * Input: FloatArray from the classifier
     * Output: The biggest value as integer
     *
     * */
    private fun getOutputInt(output: FloatArray): Int {
        return output.indices.maxBy { output[it] } ?: -1
    }

    /**
     * The getBitmapFromAsset() function ...
     *
     * Input:
     * Output:
     *
     * */
    fun getBitmapFromAsset(context: Context, filename: String) : Bitmap{
        val astMng: AssetManager = context.assets
        val inpstr: InputStream
        lateinit var bitmap: Bitmap
        try {
            inpstr = astMng.open(filename)
            bitmap = BitmapFactory.decodeStream(inpstr)
        } catch ( e: IOException ) {
            // handle exception here
        }
        return bitmap
    }

    fun close() {
        interpreter?.close()
    }

    companion object {
        private const val TAG = "DigitClassifier"
        //private const val MODEL_FILE = "mnist_model2.tflite"
        private const val FLOAT_TYPE_SIZE = 4
        private const val PIXEL_SIZE = 1
        private const val OUTPUT_CLASSES_COUNT = 20 // to 10
    }
}