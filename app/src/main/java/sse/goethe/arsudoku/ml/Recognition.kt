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

    /* Sudokus 4 edge coordinates */
    private var edgeCoordinates: Array<Array<Int>>
        get() { return edgeCoordinates }

    private lateinit var sudokuMid: Array<Int>

    /* 81 dim array with classes 0..19 */
    private var digitPredictions: Array<Int>

    /* This cropped Sudoku blocks will go to the classifier */
    private lateinit var croppedSudokuBlocks: Array<Bitmap>

    /* DEFINE AS WELL */
    // Instance of DigitClassifier
    // Array of cropped Bitmaps

    init {
        edgeCoordinates = arrayOf(arrayOf(0,0), arrayOf(0,0), arrayOf(0,0), arrayOf(0,0))
        digitPredictions = Array(81) {i -> 0}

        // Initialize Interpreter from DigitClassifier
    }

}