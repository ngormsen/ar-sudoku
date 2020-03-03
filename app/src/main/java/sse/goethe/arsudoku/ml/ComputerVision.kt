/**
 * @author Christopher Wiesner
 * date: 29.02.2020
 */
package sse.goethe.arsudoku.ml

import android.graphics.Bitmap

/**
 * Description: ...
 */
class ComputerVision {

    /* values and variables */
    private lateinit var bitmap: Bitmap

    /* init is called once if class is instantiated */
    init {
        /* don't forgett to init lateinit variables */
    }

    /* class functions */

    /* Get Bitmap of Sudoku and crop to 81 Bitmaps */
    fun cropSudoku(sudokuImg: Bitmap):Array<Bitmap> {
        var tmp: Array<Bitmap> = arrayOf(bitmap)
        // Get Sudoku from Stream
        // convert into Bitmap
        // and crop with some OCV functions
        // croppedSudokuBlocks = ...
        return tmp
    }

    /* Take 4 int edge coordinates and return Array with mid coordinates */
    fun calculateSudokuMid(coordinates: Array<Array<Int>>): Array<Int> {
        // take the 4 edge coordinates
        // and calculate the midpoint
        // (x1, y1), (x2, y2), (x3, y3), (x4, y4)
        // (x2 - x1) / 2 and
        // (y1 - y3) / 2
        // sudokuMidCoordinates
        return arrayOf(1)
    }

    /* in case of using an array for sudoku */
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