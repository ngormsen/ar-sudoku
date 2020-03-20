package sse.goethe.arsudoku

import org.opencv.core.Core.inRange
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class Visualisation {

    // colums and row size
    private val TOTAL_ROWS = 9
    private val TOTAL_COLS = 9

    fun visualisation(inputMat: Mat, sudokuMat: Mat, transformMat :  Mat, digits : Array<Array<Int>>) : Mat {

        val outputSize = inputMat.size()                                                      // the output has the same size like the input
        var edit_sudoku = renderDigits(sudokuMat, digits)                                      // edit_sudoku is a Mat with the redered digits
        edit_sudoku = rotateMat(edit_sudoku)                                                        // rotate the sudoku bc of rotated CameraView
        edit_sudoku = perspectiveTransform(inputMat, edit_sudoku, transformMat)                     // perspective transformation
        val mask = createMask(edit_sudoku)                                                     // create mask
        edit_sudoku = mergeMat(inputMat, edit_sudoku, mask)
        return edit_sudoku
    }

    // Write/render solved digits on the cropped sudoku
    private fun renderDigits (sudokuMat : Mat, digits : Array<Array<Int>>) : Mat {

        val sudoku_matSize = sudokuMat.size()
        val sudoku_matType = sudokuMat.type()
        var canvas = Mat.zeros(sudoku_matSize, sudoku_matType)

        val cellWidth = sudokuMat.width() / 9

        for (row in 0 until TOTAL_ROWS) {
            for (col in 0 until TOTAL_COLS) {
                val digit = digits[row][col].toString()
                if (digit != null) {
                    val x = row * cellWidth.toDouble()
                    val y = col * cellWidth.toDouble()
                    Imgproc.putText(canvas,
                                    digit,
                                    Point(x,y),
                            3,
                            1.0,
                                    Scalar(0.0,0.0,0.0))
                }
            }
        }
        return canvas
    }

    // TODO
    // merge the edit sudoku with the video stream
    private fun mergeMat (inputMat : Mat, sudokuMat: Mat, mask : Mat) : Mat {

        var outputMat = inputMat.clone()

        sudokuMat.copyTo(outputMat, mask)
        return outputMat
    }

    // TODO
    // Rotate the matrix
    private fun rotateMat(canvas: Mat) : Mat {

        return canvas
    }

    // TODO
    // Perspective transformation
    private fun perspectiveTransform (inputMat: Mat, sudokuMat: Mat, transformMat :  Mat) : Mat {

        val matSize = inputMat.size()
        val matType = inputMat.type()
        var outputMat = Mat.zeros(matSize, matType)

        Imgproc.warpPerspective(inputMat, outputMat, transformMat, matSize)
        return outputMat
    }


    /*
            https://stackoverflow.com/questions/45131216/opencv-overlay-two-mat-drawings-not-images-with-transparency
     */
    private fun createMask (sudokuMat: Mat) : Mat {

        var mask = sudokuMat.clone()
        inRange(sudokuMat,Scalar(0.0,0.0,0.0,0.0), Scalar(0.0,0.0,0.0,0.0), mask)

        return mask
    }
}