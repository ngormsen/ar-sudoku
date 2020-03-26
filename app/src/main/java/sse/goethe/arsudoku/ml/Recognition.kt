/**
 * @author David Machajewski
 * @author Christopher Wiesner
 * date: 19.02.2020
 *
 */
package sse.goethe.arsudoku.ml
import sse.goethe.arsudoku.ml.ComputerVision
import sse.goethe.arsudoku.ml.DigitClassifier
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Camera
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import com.google.common.primitives.UnsignedBytes.toInt
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.Utils
import org.opencv.core.Core.bitwise_not
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import sse.goethe.arsudoku.MainActivity
import java.io.*
import java.lang.IllegalStateException
import kotlin.math.floor

/**
 * The Recognition Class instantiate the DigitClassifier
 * and the OpenCV Module and manages their interaction and
 * their dependence to the stream of the camera.
 */
class Recognition(context: Context) {
    var context = context
    private var digitClassifier = DigitClassifier(context)
    var computerVision = ComputerVision()
    var sudokuIsExistent: Boolean = false // For Kelvin <- wird nicht mehr benötigt
    // The whole squared Sudoku
    lateinit var sudokuSquare: Mat // For Kelvin <- wird nicht mehr benötigt

    var sudokuEdgeCoordinates: Array<Array<Int>> // Sudokus 4 edges
        get() { return sudokuEdgeCoordinates }
        private set
    var sudokuCellMidCoordinates: Array<Point> // 81 coords
        get() { return sudokuCellMidCoordinates }
        private set

    /**
     * 81 dim array with classes 0..19
     * 0 = empty field, 1-9 machine written, 10 - 18 hand written
     * */
    var sudokuPredictedDigits: Array<Array<Int>>
    var sudokuHandOrMachinePrintedFields: Array<Array<Int>>

    lateinit var croppedSudokuMats: Array<Mat>
    private lateinit var croppedSudokuBlocksBinary: Array<Bitmap>
    private lateinit var croppedSudokuBlocks: Array<Bitmap>

    lateinit var testbitmap: Bitmap

    init {
        // x1 top left corner, x2 top right corner, x3 bottom left ...
        sudokuEdgeCoordinates = arrayOf(    arrayOf(700, 2000), arrayOf(1200,2000),
                                            arrayOf(700, 1500), arrayOf(1200, 1500) )

        sudokuCellMidCoordinates = Array(81) { Point(1.0,1.0) }

        sudokuPredictedDigits = arrayOf(
                                    arrayOf(5, 0, 0, 3, 0, 1, 0, 0, 7),
                                    arrayOf(0, 1, 0, 4, 0, 6, 0, 9, 0),
                                    arrayOf(0, 0, 8, 0, 5, 0, 4, 0, 0),
                                    arrayOf(1, 7, 0, 0, 0, 0, 0, 5, 9),
                                    arrayOf(0, 0, 6, 0, 0, 0, 7, 0, 0),
                                    arrayOf(4, 2, 0, 0, 0, 0, 0, 8, 3),
                                    arrayOf(0, 0, 4, 0, 2, 0, 3, 0, 0),
                                    arrayOf(0, 8, 0, 7, 0, 5, 0, 2, 0),
                                    arrayOf(2, 0, 0, 9, 0, 4, 0, 0, 5) )

        /**
         * -1 = False if it is machine printed
         *  1 = True if it is hand written
         *  0 = empty if it is an empty field
         * */
        sudokuHandOrMachinePrintedFields = arrayOf(
                                    arrayOf(-1, 0, 0, -1, 0, -1, 0, 0, -1),
                                    arrayOf(0, -1, 0, -1, 0, -1, 0, -1, 0),
                                    arrayOf(0, 0, -1, 0, -1, 0, -1, 0, 0),
                                    arrayOf(-1, -1, 0, 0, 0, 0, 0, -1, -1),
                                    arrayOf(0, 0, -1, 0, 0, 0, -1, 0, 0),
                                    arrayOf(-1, -1, 0, 0, 0, 0, 0, -1, -1),
                                    arrayOf(0, 0, -1, 0, -1, 0, -1, 0, 0),
                                    arrayOf(0, -1, 0, -1, 0, -1, 0, -1, 0),
                                    arrayOf(-1, 0, 0, -1, 0, -1, 0, 0, -1) )

        /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        * Set up the digit classifier
        *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
        digitClassifier.initializeInterpreter()
        testbitmap = digitClassifier.getBitmapFromAsset(context, "mnist_self_1.png")

        //var predictedClass: Int = digitClassifier.classify(testbitmap)
        //Log.d(TAG, "The predicted class is: " + predictedClass)
    }

    /**
     * The run() function is the final wrapper function which
     * combines the recognition and inference logic.
     *
     * More description: ...
     *
     * Input:
     * Output:
     *
     * */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun run(frame: CameraBridgeViewBase.CvCameraViewFrame) {
        computerVision.analyzeFrame(frame)
        //croppedSudokuMats = computerVision.SudokuBoxes!!

        /* keep this one! */
        //croppedSudokuBlocks = computerVision.SudokuBoxesBitmap!!

        for (i in 0..80) {
            /* test of threadsafe classifying  */

            /*
            if ( (croppedSudokuBlocks[i] != null ) && digitClassifier.isInitialized ) {
                digitClassifier
                    .classifyAsynchronous( croppedSudokuBlocks[i] )
                    .addOnSuccessListener { Log.d("Recognition", "inferenced number from " + "block " + i + ": " + digitClassifier.classify(croppedSudokuBlocks[i])) }
            }
            Log.d("Recognition", "Error classifying")
            */

            /* End test of threadsafe classyfying */
            //Log.d("Recognition", "inferenced number from " + "block " + i + ": " + digitClassifier.classify(croppedSudokuBlocks[i]))
        }

        //Log.d("Recognition:", "test inference: " + digitClassifier.classify( testbitmap ) ) // works for mnist!
        //Log.d("Recognition:", "test inference: " + digitClassifier.classify( croppedSudokuBlocks[0] ) )
        //classifyAll()
    }

    /**
     * The classify function classifies a machine or handwritten digit or
     * a empty field into 19 classes. Change to 20
     *
     * Input: cropped Bitmap of a Sudoku cell
     * Output: classified digit
     *
     * */
    private fun classifyAll() {
        var count = 1
        if (croppedSudokuBlocks!!.size != 81) { throw IllegalStateException(" croppedSudokuBlock has not size 81 yet. ") }

        for (sudokuBlock in this!!.croppedSudokuBlocks!!) {
            var recogDigit = digitClassifier.classify(sudokuBlock)
            var blockCoord = calculateSudokuDigitCells(count)
            addToResultMatrix(blockCoord, recogDigit)
            // TODO: as well save if it is machine printed or hand written
            count++
        }
    }

    /** Use this function to validate over multiple frames
     *  if the inference is correct
     *
     *  Input:
     *  Output:
     *
     * */
    private fun digitRecognitionIsValid(): Boolean{
        // This function has to validate the classification.
        // Take another x frames and
        // run the inference again
        // Only if all x frames inferred the same classification
        // provide the data to AR and solver
        return false
    }

    /**
     * The addToResultMatrix add a class infered by classify()
     * to the sudokuPredictedDigit Matrix.
     *
     * Input: Array with 2 Elements sybolizing the coordinates
     *        The interpreted result from the digitClassifier
     * Output: void
     *
     * */
    private fun addToResultMatrix(coordinate: Array<Int>, result: Int) {
        // TODO: IS sudokuPredictedDigits Matrix initialized?!?!?!
        // TODO: As well add if it is machine, hand printed/written -1, 0, 1 to sudokuHandOrMachinePrintedFields
        sudokuPredictedDigits[coordinate[0]][coordinate[1]] = result
    }

    /* in case of using an array for sudoku */
    /**
     * The calculateSudokuDigitCells just transforms the array position of
     * a digit to the 2 dim cell positions within the sudoku 81x81 field.
     *
     * Input: Index/Place within an array ( 1,...,9,...,81 )
     * Output: Array with coordinate.
     *         First position row,
     *         second position column.
     *
     * */
    private fun calculateSudokuDigitCells(index: Int): Array<Int> {
        val SIZE = 9
        var row = 0
        var column = index%SIZE - 1
        if (index%SIZE == 0) { row = index/SIZE - 1 }
        else { row = floor((index/SIZE).toDouble()).toInt() }
        return arrayOf(row, column)
    }

    private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
        outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }

    private fun saveBitmapPng(context: Context, bitmap: Bitmap, name: String): Uri{
        Log.d(TAG, "saveBitmapPng()")

        var filename = "output_" + name + ".png"
        //var sudokuDirectory = File("/DCIM/sudoku/")
        var sudokuDirectory = File(Environment.getDataDirectory().toString() + "/DCIM/" + "/sudoku/")
        //var sudokuDirectory = File(context.filesDir, "sudokuFolder")
        sudokuDirectory.mkdirs()
        val fOut = File(sudokuDirectory, filename)
        try {
            var stream: ByteArrayOutputStream = ByteArrayOutputStream()
            var outputStream: FileOutputStream = FileOutputStream(fOut, true)
            // Compress bitmap  or Bitmap.CompressFormat.PNG
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            outputStream.write(stream.toByteArray())
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(fOut.absolutePath)
    }


    private fun applyThresholdToBoxes(sudokuBoxesMat: Array<Mat>): Array<Mat>{
        // threshold like MNIST Dataset
        var destination = sudokuBoxesMat
        var destinationRvrs = sudokuBoxesMat
        for (i in sudokuBoxesMat.indices) {
            Imgproc.adaptiveThreshold(sudokuBoxesMat[i], destination[i],
                256.0,
                Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY,
                10,
                40.0)
            bitwise_not(destination[i], destinationRvrs[i])
        }
        return destination
    }

    private fun convertMatBmp(mats: Array<Mat>): Array<Bitmap> {
        var boxesBitmap: Array<Bitmap> = Array<Bitmap>(81) {
            createBitmap(mats[0].width(), mats[0].height())
        }

        for (i in mats.indices){
            boxesBitmap[i] = cnv(mats[i])
        }
        return boxesBitmap
    }

    private fun cnv(frameMat: Mat): Bitmap {
        val bmp: Bitmap = Bitmap.createBitmap(frameMat.cols(), frameMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(frameMat, bmp)
        return bmp
    }

    fun close() {
        digitClassifier.close()
    }

    companion object {
        private const val TAG = "Recognition"
    }
}