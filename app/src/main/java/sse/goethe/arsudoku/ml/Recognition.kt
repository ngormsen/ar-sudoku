/**
 * @author David Machajewski
 * @author Christopher Wiesner
 * date: 19.02.2020
 *
 *
 */
package sse.goethe.arsudoku.ml
import sse.goethe.arsudoku.ml.ComputerVision
import sse.goethe.arsudoku.ml.DigitClassifier
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import sse.goethe.arsudoku.MainActivity

/**
 * The Recognition Class instantiate the DigitClassifier
 * and the OpenCV Module and manages their interaction and
 * their dependence to the stream of the camera.
 */
class Recognition(private val context: Context) {
    /*+++++++++++++++++++++++++++++++++++++++
    * Variables and Values
    *++++++++++++++++++++++++++++++++++++++++ */

    private var digitClassifier = DigitClassifier(context)


    /* Sudokus 4 edge coordinates */
    var sudokuEdgeCoordinates: Array<Array<Int>>
        get() { return sudokuEdgeCoordinates }
        private set

    var sudokuMidCoordinates: Array<Int>
        get() { return sudokuMidCoordinates }
        private set

    /* 81 dim array with classes 0..19
       0 = empty field, 1-9 machine written, 10 - 18 hand written */
    var sudokuPredictedDigits: Array<Array<Int>>
    var sudokuFieldIsHandwritten: Array<Array<Int>>

    /* This cropped Sudoku blocks will go to the classifier */
    private lateinit var croppedSudokuBlocks: Array<Bitmap>

    /* DEFINE AS WELL */
        // Instance of DigitClassifier
        // Array of cropped Bitmaps

    init {
        // x1 top left corner, x2 top right corner, x3 bottom left ...
        sudokuEdgeCoordinates = arrayOf(    arrayOf(700, 2000), arrayOf(1200,2000),
                                            arrayOf(700, 1500), arrayOf(1200, 1500) )

        sudokuMidCoordinates = arrayOf( 950, 1750 )

        /* initialize sudokuPredictedDigits array with 0 */
        /*
        for (i in 0..8) {
            var tmp = arrayOf<Int>()
            for (j in 0..8) { tmp += 0 }
            this.sudokuPredictedDigits += tmp
        }
        */

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
        /* -1 = False if it is machine printed
        *  1 = True if it is hand written
        *  0 = empty if it is an empty field */
        sudokuFieldIsHandwritten = arrayOf(
            arrayOf(-1, 0, 0, -1, 0, -1, 0, 0, -17),
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

        //digitClassifier.initializeInterpreter()
        /* test with a bitmap */
        //var testbitmap: Bitmap = digitClassifier.getBitmapFromAsset(context, "mnist_7.PNG")
        // Initialize Interpreter from DigitClassifier

        //var predictedClass: String = digitClassifier.classify(testbitmap)
        //Log.d(Recognition.TAG, "The predicted class is: " + predictedClass)

    }

    /** ++++++++++++++++++++++++++++++++++++
     * CLASS FUNCTIONS
     +++++++++++++++++++++++++++++++++++++++*/

    fun close(){
        digitClassifier.close()
    }

    companion object {
        // just to use for Log's
        private const val TAG = "Recognition"
    }
}