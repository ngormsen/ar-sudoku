/**
 *  @author Kelvin Tsang
 */
package sse.goethe.arsudoku

import android.util.Log
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.*
import org.opencv.core.Core.*
import org.opencv.imgproc.Imgproc
import sse.goethe.arsudoku.ml.Recognition

/**
 *  @author Kelvin Tsang
 *
 *  This class does the visualisation.
 *  It renders digits und merges the edit sudoku with the camera view.
 */
class Visualisation(recognition: Recognition) {

    private val TAG : String = "Visualisation"

    // colums and row size
    private val n = 9

    // mat attributes
    private val SUDOKU_MAT_SIZE : Int = 900
    private val matType = 24 // https://ninghang.blogspot.com/2012/11/list-of-mat-type-in-opencv.html

    // font attributes
    private val FONT_FACE = FONT_HERSHEY_SIMPLEX // https://codeyarns.github.io/tech/2015-03-11-fonts-in-opencv.html
    private val FONT_SCALE = 2.7
    private val FONT_THICKNESS = 2
    private val FONT_LINETYPE = LINE_AA

    // rotate counter clockwise
    private val ROTANTION_ANGLE = 90.0

    // colour as Scalar(RED, Green, BLUE)
    private val BLACK = Scalar(0.0, 0.0,0.0, 0.0)
    private val WHITE = Scalar(255.0, 255.0,255.0, 0.0)
    private val RED = Scalar(255.0, 0.0,0.0, 0.0)
    private val GREEN = Scalar(0.0, 255.0,0.0, 0.0)
    private val BLUE = Scalar(0.0, 0.0,255.0, 0.0)

    private var digitColour = RED

    // connection to Recognition and ComputerVision
    private var recognition: Recognition = recognition

    private lateinit var inputMat : Mat
    private lateinit var transformMat : Mat
    private lateinit var digits : Array<IntArray>

    private var sudokuCorners : MatOfPoint2f? =  null

    private lateinit var inputSize : Size

    private lateinit var sudoku_mask : Mat
    private lateinit var outputMat_mask : Mat
    private lateinit var outputMat : Mat

    private var startTime : Long = 0

    /**
     *  This function starts the visualisation if sudoku corners are found.
     *  It creates the sudoku mask and the outputMat and merges it with the inputMat
     *  @param inputFrame is the camera input of the camera view
     *  @param solvedSudoku is a array with the digits of the solved sudoku
     *
     *  @return edit input with rendered digits
     */
    fun run(inputFrame: CameraBridgeViewBase.CvCameraViewFrame, solvedSudoku : Array<IntArray>) : Mat {

        inputMat = inputFrame.rgba()
        inputSize = inputMat.size()
        outputMat = Mat.zeros(inputSize, matType)
        sudoku_mask = Mat.zeros(inputSize, matType)

        digits = solvedSudoku
        sudokuCorners = recognition.computerVision.SudokuCorners

        return if (getTransformationMat()) {
            createSudokuMask()
            createOutput(digitColour)
            mergeMat()
            outputMat
        }
        else inputMat
    }

    /**
     *  This public function checks if the computer vision part found sudoku corners
     *  and calculates the transformation matrix with the help of a corners of a square and the corners of the sudoku.
     *  The transformation matrix is for the perspective transformation.
     */
    private fun getTransformationMat () : Boolean {
        return if (sudokuCorners != null) {
            val sudokuCoords: MatOfPoint2f = (MatOfPoint2f(
                Point(0.0,0.0),
                Point(SUDOKU_MAT_SIZE.toDouble(), 0.0),
                Point(0.0, SUDOKU_MAT_SIZE.toDouble()),
                Point(SUDOKU_MAT_SIZE.toDouble(), SUDOKU_MAT_SIZE.toDouble())
                ))

            transformMat =  Imgproc.getPerspectiveTransform(sudokuCoords, sudokuCorners)
            true
        }
        else false
    }

    /**
     *  This private function creates a sudoku mask.
     *  It renders digits on a square mat and does a perspective transformation.
     *  The sudoku mask has white digits and an black blackground.
     *
     *  https://stackoverflow.com/questions/45131216/opencv-overlay-two-mat-drawings-not-images-with-transparency
     */
    private fun createSudokuMask () {

        sudoku_mask = renderDigits()
        sudoku_mask = transformPerspective()
    }

    /**
     *   This private function creates a mat with size SUDOKU_MAT_SIZE
     *   and renders the digits on their location in white colour.
     *   @param FONT_COLOUR is the font colour. Default colour is white
     *   @param BACKGROUND_COLOUR is the background colour. Default colour is black
     *
     *   @return rotate mat with rendered digits
     */
    private fun renderDigits (FONT_COLOUR : Scalar = WHITE, BACKGROUND_COLOUR : Scalar = BLACK) : Mat{

        var canvas = Mat.zeros(SUDOKU_MAT_SIZE, SUDOKU_MAT_SIZE, matType)
        if (BACKGROUND_COLOUR != BLACK) canvas.setTo(BACKGROUND_COLOUR)

        val cellWidth = SUDOKU_MAT_SIZE / n

        for (row in 0 until n) {
            for (col in 0 until n) {
                val digit = digits[row][col].toString()
                if (digit != null && digit != "0") {
                    val x = col * cellWidth.toDouble() + cellWidth*0.22
                    val y = (row+1) * cellWidth.toDouble() - cellWidth*0.22

                    Imgproc.putText(canvas, digit, Point(x,y), FONT_FACE, FONT_SCALE, FONT_COLOUR, FONT_THICKNESS, FONT_LINETYPE)
                }
            }
        }
        /**
         *      FOR TESTING -> DRAW SUDOKU GRID
         */
        //canvas = drawGrid(canvas)

        return rotateMat(canvas)
    }

    /**
     *  This private function rotates the mat by 90 degrees by default.
     *  @param input is the mat that rotetes
     *  @param angle is the rotation angle. Default value is 90 degrees
     *
     *  https://stackoverflow.com/questions/15043152/rotate-opencv-matrix-by-90-180-270-degrees
     */
    private fun rotateMat (input : Mat, angle : Double = ROTANTION_ANGLE) : Mat {

        val centerPoint : Point = Point(input.cols()/2.0, input.rows()/2.0)
        val rotMat : Mat = Imgproc.getRotationMatrix2D(centerPoint, angle, 1.0)
        var dst : Mat = Mat.zeros(input.cols(), input.rows(), input.type())

        Imgproc.warpAffine(input, dst, rotMat, input.size(), Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT)

        return dst
    }

    /**
     *  This private function merges the inputMat with the outputMat with help of the sudoku mask.
     *  @param input is the camera input as mat
     *  @param mask is by default the sudoku mask
     */
    private fun mergeMat (input : Mat = inputMat, mask : Mat = sudoku_mask) {

        val dst : Mat = input
        outputMat.copyTo(dst, mask)
        outputMat = dst
    }

    /**
     *  This private function does the perspective transformation of the sudoku with help of the transformation matrix
     *  @param input is by default sudoku mask
     */
    private fun transformPerspective (input : Mat = sudoku_mask) : Mat{

        var dst : Mat = Mat.zeros(inputSize, matType)
        Imgproc.warpPerspective(input, dst, transformMat!!, inputSize, Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT)

        return dst
    }

    /**
     *  This private function creates the output mat with help of the sudoku mask.
     *  It creates a white mat and change the colour in the location of the mask where it is white.
     *  @param colour is the colour of the digits
     */
    private fun createOutput (colour : Scalar) {

        val whiteMat : Mat = createColouredMat()
        outputMat = whiteMat.setTo(colour, sudoku_mask)
    }

    /**
     *  This private function creates a coloured mat with the size of the input frame
     *  @param colour is by default white
     *
     *  @return colored mat
     */
    private fun createColouredMat (colour : Scalar = WHITE) : Mat {

        val dst : Mat = Mat.zeros(inputSize, matType)

        return dst.setTo(colour)
    }

    /**
     *  Function TODO
     *
     */
    private fun createOutputMask () : Mat {
        var canvas = Mat.zeros(SUDOKU_MAT_SIZE, SUDOKU_MAT_SIZE, matType)
        return transformPerspective(canvas)
    }

    /**
     *  This public function changes the rendered digit colour
     *  @param colour
     */
    fun setDigitColour (colour : Scalar) {

        digitColour = colour
    }

    /**
     *  FOR TESTING
     *
     *  This private function draws a sudoku grid on mat.
     *  @param input
     *  @param colour
     *  @param thickness
     */
    private fun drawGrid(input : Mat = sudoku_mask, colour : Scalar = WHITE, thickness : Int = 2) : Mat {

        val height = input.height().toDouble()-1
        val width = input.width().toDouble()-1
        val cellHeight = (input.height()/9).toDouble()
        val cellWidth = (input.height()/9).toDouble()

        var output : Mat = input

        // draw frame
        Imgproc.rectangle(output, Point(0.0, 0.0), Point(width, height), colour, thickness, LINE_AA)
        // draw inner grid
        for (i in 1 until n) {
            val y = i * cellHeight
            val x = i * cellWidth
            Imgproc.line(output, Point(0.0, y), Point(width, y), colour, thickness, LINE_AA)
            Imgproc.line(output, Point(x, 0.0), Point(x, height), colour, thickness, LINE_AA)
        }

        return output
    }

    /**
     *  FOR TESTING
     *
     *  This private function a mat to another size
     *  @param input is the mat which is to resize
     *  @param output is the output size
     *
     *  @return resized mat
     */
    private fun resizeMat (input : Mat, output : Mat) : Mat {

        var dst : Mat = Mat.zeros(output.size(), output.type())
        Imgproc.resize(input, dst, output.size())

        return dst
    }

    /**
     *  FOR TESTING
     *
     *  This purblic function starts the timer.
     */
    fun startTime() {
        startTime = System.nanoTime()
    }

    /**
     *  FOR TESTING
     *
     *  This public function stops the timer.
     */
    fun stopTime(tag : String = TAG, start : Long = startTime) {
        var elapsedTime = (System.nanoTime() - start).toDouble() / 1000000
        Log.d(tag, elapsedTime.toString() + " ms")
    }
}