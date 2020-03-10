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
import android.util.Log
import org.tensorflow.lite.Interpreter
import sse.goethe.arsudoku.ml.Keys.MODEL_PATH
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.io.*

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

    /**
     * The initializeInterpreter() function has to be called
     * before you launch the interpreter.run().
     *
     * Do not forget to close() the Interpreter
     * */
    fun initializeInterpreter() {
        // load model
        val assetManager = context.assets
        val model = loadModelFile(assetManager)

        // Init TF Lite Interpreter with NNAPI enabled
        val options = Interpreter.Options()
        options.setUseNNAPI(true)
        val interpreter = Interpreter(model, options)

        // Read input shape from model file
        val inputShape = interpreter.getInputTensor(0).shape()
        inputImageWidth = inputShape[1]
        inputImageHeight = inputShape[2]
        modelInputSize = FLOAT_TYPE_SIZE * inputImageWidth * inputImageHeight * PIXEL_SIZE

        this.interpreter = interpreter
        isInitialized = true
        Log.d(TAG, " >> Initialized TFLite interpreter! << ")

    }

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

            // convert RGB to grayscale and normalize it
            val normalizedPixelValue = (r + g + b) / 3.0f / 255.0f
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
    //fun classify(bitmap: Bitmap): String {
    fun classify(bitmap: Bitmap): Int {
        if (!isInitialized) {
            throw IllegalStateException(" TF Lite Interpreter is not initialized yet. ")
        }
        // Preprocessing: resize the input
        val resizedImage = Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true)
        val byteBuffer = convertBitmapToBytebuffer(resizedImage)
        val result = Array(1) { FloatArray(OUTPUT_CLASSES_COUNT) }
        interpreter?.run(byteBuffer, result)
        //return getOutputString(result[0])
        return getOutputInt(result[0])
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
        private const val MODEL_FILE = "mnist.tflite"
        private const val FLOAT_TYPE_SIZE = 4
        private const val PIXEL_SIZE = 1
        private const val OUTPUT_CLASSES_COUNT = 10
    }
}

/*
class DigitClassifier(private val context: Context) {
    private var interpreter: Interpreter? = null
    var isInitialized = false
        private set

    /* Executor which runs the inference in the background */
    private val executorService: ExecutorService = Executors.newCachedThreadPool()
    private var inputImageWidth: Int = 0
    private var inputImageHeight: Int = 0
    private var modelInputSize: Int = 0

    fun initialize(): Task<Void> {
        return call(
            executorService,
            Callable<Void> {
                initializeInterpreter()
                null
            }
        )
    }

    @Throws(IOException::class)
    private fun initializeInterpreter() {
        // Load the TF model
        val assetManager = context.assets
        val model = loadModelFile(assetManager)

        // Initialize TF Lite Interpreter with NNAPI enabled
        val options = Interpreter.Options()
        options.setUseNNAPI(true)
        val interpreter = Interpreter(model, options)

        // Read input shape from model file
        val inputShape = interpreter.getInputTensor(0).shape()
        inputImageWidth = inputShape[1]
        inputImageHeight = inputShape[2]
        modelInputSize = FLOAT_TYPE_SIZE * inputImageWidth * inputImageHeight * PIXEL_SIZE

        // Finish interpreter initialization
        this.interpreter = interpreter
        isInitialized = true
        Log.d(TAG, "Initialized TFLite interpreter")
    }

    @Throws(IOException::class)
    private fun loadModelFile(assetManager: AssetManager): ByteBuffer {
        val fileDescriptor = assetManager.openFd(MODEL_PATH)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun classify(bitmap: Bitmap): String {

        if (!isInitialized) {
            throw IllegalStateException("TF Lite Interpreter is not initialized yet")
        }

        var startTime = nanoTime()

        // Preprocessing resize the input
        val resizedImage = Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true)
        val byteBuffer = convertBitmapToByteBuffer(resizedImage)

        val result = Array(1){FloatArray(OUTPUT_CLASSES_COUNT)}
        interpreter?.run(byteBuffer, result)

        var elapsedTime = (nanoTime() - startTime) / 1000000
        Log.d(TAG, "Preprocessing time = " + elapsedTime + "ms")

        return getOutputString(result[0])
    }

    fun classifyAsync(bitmap: Bitmap): Task<String> {
        return call(executorService, Callable<String> {classify(bitmap)})
    }

    fun close(){
        call(
            executorService,
            Callable<String> {
                interpreter?.close()
                Log.d(TAG, "Closed TFLite interpreter.")
                null
            }
        )
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer{
        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputImageWidth * inputImageHeight)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in pixels) {
            val r = (pixelValue shr 16 and 0xFF)
            val g = (pixelValue shr 8 and 0xFF)
            val b = (pixelValue and 0xFF)

            // convert RGB to grayscale and normalize pixel value
            val normalizedPixelValue = (r+g+b)/ 3.0f / 255.0f
            byteBuffer.putFloat(normalizedPixelValue)
        }
        return byteBuffer
    }

    private fun getOutputString(output: FloatArray): String {
        val maxIndex = output.indices.maxBy { output[it] } ?: -1
        return "Prediction Result: %d\nConfidence: %2f".format(maxIndex, output[maxIndex])
    }

    companion object {
        private const val TAG = "DigitClassifier"
        private const val FLOAT_TYPE_SIZE = 4
        private const val PIXEL_SIZE = 1
        private const val OUTPUT_CLASSES_COUNT = 10
    }
}

*/














/*
class DigitClassifier constructor(private val assetManager: AssetManager) {
    private var interpreter: Interpreter? = null
    val results = 0
    private var labelProb: Array<ByteArray>
    private val labels = Vector<String>()
    private var imgData: ByteBuffer
    private val intValues by lazy { IntArray(INPUT_SIZE) }

    init {
        /*
        During an instance initialization, the initializer blocks
        are executed in the same order as they appear in the class body
        */
        Log.i("CLASSIFIER CLASS", "TEST CLASSIFIER CLASS")

        try {
            /* LOAD LABELS */
            val br = BufferedReader(InputStreamReader(assetManager.open(LABEL_PATH)))
            while (true) {
                val line = br.readLine() ?: break
                labels.add(line)
            }
            br.close()
        }
        catch (e: IOException) {
            throw RuntimeException("can not read the labels or label file!", e)
        }

        // TODO: Init ByteArray, get image data and load the Model
        labelProb = Array(1) {ByteArray(labels.size)}

        imgData = ByteBuffer.allocateDirect(DIM_BATCH_SIZE * DIM_PIXEL_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y)
        imgData.order(ByteOrder.nativeOrder())
        try {
            /* LOAD MODEL */
            interpreter = Interpreter(loadModelFile(assetManager, MODEL_PATH))
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun loadModelFile(assets: AssetManager, modelFilename: String): MappedByteBuffer {
        Log.i("loadAsset", "Function: loadModelFile")

        val fileDescriptor = assets.openFd(modelFilename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

        /* TODO: Test if it is working properly with some prints ! */
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap) {
        imgData.rewind()
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        // getPixels() returns the complete int[] array of the source bitmap,
        // so has to be initialized with the same length as the source bitmap's height x width
        var pixel = 0
        for(i in 0 until DIM_IMG_SIZE_X) {
            for (j in 0 until DIM_IMG_SIZE_Y) {
                val value = intValues[pixel++]
                imgData.put((value shr 16 and 0xFF).toByte()) // r
                imgData.put((value shr 8 and 0xFF).toByte()) // g
                imgData.put((value and 0xFF).toByte()) // b


            }
        }
    }

    fun recogImage(bitmap: Bitmap): Int {
        /* TODO: interpreter will take the imgData as bytebuffer ... */
        interpreter!!.run(imgData, labelProb) // !! throws a null pointer exception

        return 1
    }

    /*
    fun recognizeImage(bitmap: Bitmap): Single<List<Result>> {
        return Single.just(bitmap).flatMap {
            convertBitmapToByteBuffer(it)
            interpreter!!.run(imgData, labelProb)
            val pq = PriorityQueue<Result>(3,
                Comparator<Result> { lhs, rhs ->
                    // Intentionally reversed to put high confidence at the head of the queue.
                    java.lang.Float.compare(rhs.confidence!!, lhs.confidence!!)
                })
            for (i in labels.indices) {
                pq.add(Result("" + i, if (labels.size > i) labels[i] else "unknown", labelProb[0][i].toFloat(), null))
            }
            val recognitions = ArrayList<Result>()
            val recognitionsSize = Math.min(pq.size, MAX_RESULTS)
            for (i in 0 until recognitionsSize) recognitions.add(pq.poll())
            return@flatMap Single.just(recognitions)
        }
    }
    */
}

*/