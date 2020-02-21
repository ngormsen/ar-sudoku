/**
 * @author David Machajewski
 * @author Christopher Wiesner
 * date: 19.02.2020
 *
 *
 */
package sse.goethe.arsudoku.ml

import android.graphics.Bitmap

class Recognition {
    /*+++++++++++++++++++++++++++++++++++++++
    * Variables and Values
    *++++++++++++++++++++++++++++++++++++++++ */

    /* Sudokus 4 edge coordinates */
    var sudokuEdgeCoordinates: Array<Array<Int>>
        get() { return sudokuEdgeCoordinates }
        private set

    var sudokuMidCoordinates: Array<Int>
        get() { return sudokuMidCoordinates}
        private set

    /* 81 dim array with classes 0..19 */
    var sudokuPredictedDigits: Array<Int>
        get() { return sudokuPredictedDigits }
        private set

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

        /* 0 = empty field, 1-9 machine written, 10 - 18 hand written */
        sudokuPredictedDigits = arrayOf(    5, 0, 0, 3, 0, 1, 0, 0, 7,
                                            0, 1, 0, 4, 0, 6, 0, 9, 0,
                                            0, 0, 8, 0, 5, 0, 4, 0, 0,
                                            1, 7, 0, 0, 0, 0, 0, 5, 9,
                                            0, 0, 6, 0, 0, 0, 7, 0, 0,
                                            4, 2, 0, 0, 0, 0, 0, 8, 3,
                                            0, 0, 4, 0, 2, 0, 3, 0, 0,
                                            0, 8, 0, 7, 0, 5, 0, 2, 0,
                                            2, 0, 0, 9, 0, 4, 0, 0, 5   )


        // Initialize Interpreter from DigitClassifier
    }

    /** ++++++++++++++++++++++++++++++++++++
     * CLASS FUNCTIONS
     +++++++++++++++++++++++++++++++++++++++*/

    private fun cropSudoku() {
        // Get Sudoku from Stream
        // convert into Bitmap
        // and crop with some OCV functions
        // croppedSudokuBlocks = ...
    }

    private fun calculateSudokuMid() {
        // take the 4 edge coordinates
        // and calculate the midpoint
        // (x1, y1), (x2, y2), (x3, y3), (x4, y4)
        // (x2 - x1) / 2 and
        // (y1 - y3) / 2
        // sudokuMidCoordinates
    }

    private fun calculateSudokuDigitCells() {
        // just calculates the array position of
        // a digit to the 2 dim cell positions
        // within the sudoku 81x81 field

        // use modulo: e.g.
        // 10 mod 9 = 1
        // 11 mod 9 = 2 ... for column number
        // for row number just divide
        // the array position by 9,
        // and if it is floating point number
        // then round up.
    }

}