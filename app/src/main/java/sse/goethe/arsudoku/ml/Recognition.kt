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
    private var digitClassifier = DigitClassifier(context)
    var computerVision = ComputerVision()

    var sudokuIsExistent: Boolean = false
    lateinit var sudokuSquare: Mat

    var sudokuPredictedDigits: Array<Array<Int>>
    var sudokuHandOrMachinePrintedFields: Array<Array<Int>>

    lateinit var croppedSudokuMats: Array<Mat>
    private lateinit var croppedSudokuBlocksBinary: Array<Bitmap>
    private lateinit var croppedSudokuBlocks: Array<Bitmap>

    lateinit var testbitmap: Bitmap

    var isReady: Boolean = true // is used to signal that recognition process is ready

    var validityCounter = arrayOf<Array<Array<Int>>>()

    // parameter for running digit recognition every x frames to reduce lagginess
    val run_every_x = 10
    var frameCounter = 0

    init {
        /** Initialization of the digit classifier */
        digitClassifier.initializeInterpreter()

        /** Initialize validityCounter */
        for (i in 0..8) {
            var row = arrayOf<Array<Int>>()
            for (j in 0..8) {
                var col = arrayOf<Int>()
                for (k in 0..9) { // 10 classes!
                    col += 0
                }
                row += col
            }
            validityCounter += row
        }

        sudokuPredictedDigits = arrayOf(
            arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0) )

        /**
         * -1 = False if it is machine printed,  1 = True if it is hand written
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

        //testbitmap = digitClassifier.getBitmapFromAsset(context, "mnist_self_1.png")
        //var predictedClass: Int = digitClassifier.classify(testbitmap)
        //Log.d(TAG, "The predicted class is: " + predictedClass)
    }

    /**
     * The run() function is the final wrapper function which
     * combines the recognition and inference logic.
     *
     * More description:
     * First we run anaylzeFrame(). This must be called once fore every
     * frame before doing Character Recognition. It set computerVision's
     * class attributes. All of those as nullable, so you MUST check for
     * null value!! If there are any null values, that means no Sudoku
     * could be found.
     * If a Sudoku was (presumably) found, we continue to do Character
     * Recognition.
     *
     * Input: frame of the camera
     *
     * */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun run(frame: CameraBridgeViewBase.CvCameraViewFrame) {
        computerVision.analyzeFrame(frame)
        frameCounter++
        if (frameCounter%run_every_x == 0) {
            isReady = true
            return
        }

        // How to do null-checks:
        if (computerVision.SudokuBoxesBitmap == null) {
            isReady = true
            return
        }
        else croppedSudokuBlocks = computerVision.SudokuBoxesBitmap!!

        try {

            classifyAll()

            sudokuPredictedDigits = rotateCounterClock(sudokuPredictedDigits)
            sudokuPredictedDigits = rotateCounterClock(sudokuPredictedDigits)
            sudokuPredictedDigits = rotateCounterClock(sudokuPredictedDigits)

            /**
             * At this position we need a function that
             * ensures stability and validates the results.
             * So we have to hold on results that do
             * not change that often.
             *
             * */

            isReady = true

        } catch (e: Exception)  { // general exception.
            Log.d(TAG, "SudokuBoxesBitmap initialized or null ?")
        }
    }

    /**
     * The classify function classifies a machine or handwritten digit or
     * a empty field into 19 classes. Change to 20.
     * Get the sudokus from "croppedSudokuBlocks" array.
     *
     * Input: Optional parameter if threadsafe method has to be used. By default false.
     * Output: classified digit
     *
     * */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun classifyAll(threadsafe: Boolean = false) {
        var count = 0
        var blockCoord: Array<Int>

        if (!threadsafe) {
            try {
                for (block in croppedSudokuBlocks) {
                    var digit = digitClassifier.classify(block)
                    blockCoord = calculateSudokuDigitCells(count)
                    addResult(blockCoord, digit)
                    count++
                }
            } catch ( e: IOException ) { Log.d(TAG, "Could not classify and add Results. ") } // NICHT NÖTIG
        } else {
            for (i in 0..80) {
            /* test of threadsafe classifying  */
            if ( (croppedSudokuBlocks[i] != null ) && digitClassifier.isInitialized ) {
                digitClassifier
                    .classifyAsynchronous( croppedSudokuBlocks[i] )
                    .addOnSuccessListener {
                        Log.d("Recognition", "inferenced number from "
                                + "block " + i + ": "
                                + digitClassifier.classify(croppedSudokuBlocks[i])) }
            }
            Log.d("Recognition", "Error classifying")
            /* End test of threadsafe classyfying */
            Log.d("Recognition", "inferenced number from " + "block " + i + ": " + digitClassifier.classify(croppedSudokuBlocks[i]))
            }
        }
    }

    /**
     *
     * */
    private fun rotateCounterClock(matrix: Array<Array<Int>>): Array<Array<Int>> {
        Log.d("rotateClockwise", "started this function")
        val n = 9
        for (i in 0 until n/2) {
            for (j in i until n-i-1) {
                var tmp = matrix[i][j]
                matrix[i][j] = matrix[j][n-i-1]
                matrix[j][n-i-1] = matrix[n-i-1][n-j-1]
                matrix[n-i-1][n-j-1] = matrix[n-j-1][i]
                matrix[n-j-1][i] = tmp
            }
        }
        return matrix
    }


    /**
     * The addResult add a class infered by classify()
     * to the sudokuPredictedDigit Matrix.
     *
     * Input: Array with 2 Elements sybolizing the coordinates
     *        The interpreted result from the digitClassifier
     * Output: void
     *
     * */
    private fun addResult(coordinate: Array<Int>, result: Int) {
        var machineHandOrNothing = 0
        var digit: Int = result
        when (result) {
            in 1..9 -> {
                machineHandOrNothing = -1
                digit = result
            }
            in 11..19 -> {
                machineHandOrNothing = 1
                digit = result % 10
            }
            /* anything else */
            0 -> {
                machineHandOrNothing = 0
                digit = 0
            }
            10 -> { // empty field
                machineHandOrNothing = 0
                digit = 0
            }
        }
        sudokuPredictedDigits[coordinate[0]][coordinate[1]] = digit
        sudokuHandOrMachinePrintedFields[coordinate[0]][coordinate[1]] = machineHandOrNothing
    }


    /**
     * The calculateSudokuDigitCells just transforms the array position of
     * a digit to the 2 dim cell positions within the sudoku 81x81 field.
     *
     * Input: Index/Place within an array ( 0,...,8,...,80 )
     * Output: Array with coordinate.
     *         First position row,
     *         second position column.
     *
     * */
    private fun calculateSudokuDigitCells(index: Int): Array<Int> {
        //Log.d("calculateSudokuDigitCells:", " given index: " + index)
        val SIZE = 9
        var row: Int = 0
        var column: Int = 0
        row = (index / SIZE) // passt
        //Log.d("calculateSudokuDigitCells:", " row:" + row)
        column = index % SIZE
        //Log.d("calculateSudokuDigitCells:", " col:" + column)
        return arrayOf(row, column)
    }

    private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
        outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }

    private fun saveBitmapPng(context: Context, bitmap: Bitmap, name: String): Uri{
        //Log.d(TAG, "saveBitmapPng()")

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