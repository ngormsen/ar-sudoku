/**
 *  @author Kelvin Tsang
 */
package sse.goethe.arsudoku

import android.util.Log
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
    private val SUDOKU_MAT_SIZE : Double = 450.0
    private val SUDOKU_MAT_SIZE_2D : Size = Size(SUDOKU_MAT_SIZE, SUDOKU_MAT_SIZE)
    private val CELL_WIDTH : Double = SUDOKU_MAT_SIZE / n
    private val matType = 24 // https://ninghang.blogspot.com/2012/11/list-of-mat-type-in-opencv.html

    // font attributes
    private val FONT_FACE = FONT_HERSHEY_SIMPLEX // https://codeyarns.github.io/tech/2015-03-11-fonts-in-opencv.html
    private val FONT_SCALE = 1.45
    private val FONT_THICKNESS = 1
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

    private lateinit var inputMat : Mat // input picture as matrix
    private lateinit var transformMat : Mat // transformation matrix
    private lateinit var digits : Array<IntArray>

    private var sudokuCorners : MatOfPoint2f? =  null // location/points of the sudoku corners

    private lateinit var inputSize : Size // size of the input matrix

    private lateinit var sudoku_mask : Mat
    private lateinit var outputMat : Mat

    private var SUDOKU_CORNER_IS_NULL = true

    private var startTime : Long = 0

    /**
     *  This function starts the visualisation if sudoku corners are found.
     *  It creates the sudoku mask and the outputMat and merges it with the inputMat
     *  @param inputFrame is the camera input of the camera view
     *  @param solvedSudoku is a array with the digits of the solved sudoku
     *
     *  @return edit input with rendered digits
     */
    fun run(inputFrame : Mat, solvedSudoku : Array<IntArray>, solvable : Boolean) : Mat {

        inputMat = inputFrame
        inputSize = inputMat.size()

        if (SUDOKU_CORNER_IS_NULL || !solvable) inputMat = drawScannerFrame()

        outputMat = Mat.zeros(inputSize, matType)
        sudoku_mask = Mat.zeros(inputSize, matType)

        digits = solvedSudoku
        sudokuCorners = recognition.computerVision.SudokuCorners

        return if (getTransformationMat() && solvable) {
            createSudokuMask()
            createOutput(digitColour)
            mergeMat()
        }
        else inputMat
    }

    /**
     *  This private function checks if the computer vision part found sudoku corners
     *  and calculates the transformation matrix with the help of a corners of a square and the location of corners of the sudoku.
     *  The transformation matrix is for the perspective transformation.
     */
    private fun getTransformationMat () : Boolean {
        return if (sudokuCorners != null) {
            SUDOKU_CORNER_IS_NULL =  false

            val sudokuCoords: MatOfPoint2f = MatOfPoint2f(
                Point(0.0,0.0),
                Point(SUDOKU_MAT_SIZE, 0.0),
                Point(0.0, SUDOKU_MAT_SIZE),
                Point(SUDOKU_MAT_SIZE, SUDOKU_MAT_SIZE)
                )

            transformMat =  Imgproc.getPerspectiveTransform(sudokuCoords, sudokuCorners)
            true
        }
        else {
            SUDOKU_CORNER_IS_NULL = true
            false
        }
    }

    /**
     *  This private function creates a sudoku mask.
     *  It renders digits on a square mat and does a perspective transformation.
     *  The sudoku mask has white digits and an black blackground.
     *
     *  https://stackoverflow.com/questions/45131216/opencv-overlay-two-mat-drawings-not-images-with-transparency
     */
    private fun createSudokuMask () {

        sudoku_mask = transformPerspective(renderDigits())
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

        var canvas = Mat.zeros(SUDOKU_MAT_SIZE_2D, matType)
        if (BACKGROUND_COLOUR != BLACK) canvas.setTo(BACKGROUND_COLOUR)

        for (row in 0 until n) {
            for (col in 0 until n) {
                val digit = digits[row][col].toString()
                if (digit != null && digit != "0") {
                    val x = col * CELL_WIDTH + CELL_WIDTH*0.22
                    val y = (row+1) * CELL_WIDTH - CELL_WIDTH*0.22

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

        val centerPoint = Point(input.cols()/2.0, input.rows()/2.0)
        val rotMat : Mat = Imgproc.getRotationMatrix2D(centerPoint, angle, 1.0)
        var dst : Mat = Mat.zeros(input.cols(), input.rows(), input.type())

        Imgproc.warpAffine(input, dst, rotMat, input.size(), Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT)

        return dst
    }

    /**
     *  This private function merges the inputMat with the outputMat with help of the sudoku mask.
     *  @param input is the camera input as mat
     *  @param mask is by default the sudoku mask
     *
     *  @return merged mat
     */
    private fun mergeMat (input1 : Mat = inputMat, input2 : Mat = outputMat, mask : Mat = sudoku_mask) : Mat {

        val dst : Mat = input1
        input2.copyTo(dst, mask)

        return dst
    }

    /**
     *  This private function does the perspective transformation of the sudoku with help of the transformation matrix
     *  @param input is the mat that does the perspective transformation
     *
     *  @return perpective transformed mat
     */
    private fun transformPerspective (input : Mat) : Mat{

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
     *  @param size of the mat
     *  @param colour is by default white
     *
     *  @return colored mat
     */
    private fun createColouredMat (size : Size = inputSize, colour : Scalar = WHITE) : Mat {

        val dst : Mat = Mat.zeros(size, matType)

        return dst.setTo(colour)
    }

    /**
     *  This private function draws the scanner frame.
     *  @param size is by default the inputSize
     *  @param colour is the colour of the scanner frame
     *  @param thickness of the line
     *
     *  @return the merged input with the scanner frame
     */
    private fun drawScannerFrame (size : Size = inputSize, colour : Scalar = digitColour, thickness: Int = 5) : Mat {

        val corners = scannerFrameCorners()
        val length = (corners[1].y-corners[0].y)*0.25

        var mask = Mat.zeros(size, matType)

        // Top Right Corner
        Imgproc.line(mask, corners[0], Point(corners[0].x + length, corners[0].y), WHITE, thickness)
        Imgproc.line(mask, corners[0], Point(corners[0].x, corners[0].y + length), WHITE, thickness)

        // Top Left Corner
        Imgproc.line(mask, corners[1], Point(corners[1].x + length, corners[1].y), WHITE, thickness)
        Imgproc.line(mask, corners[1], Point(corners[1].x, corners[1].y - length), WHITE, thickness)

        // Buttom Left Corner
        Imgproc.line(mask, corners[2], Point(corners[2].x - length, corners[2].y), WHITE, thickness)
        Imgproc.line(mask, corners[2], Point(corners[2].x, corners[2].y - length), WHITE, thickness)

        // Buttom Right Corner
        Imgproc.line(mask, corners[3], Point(corners[3].x - length, corners[3].y), WHITE, thickness)
        Imgproc.line(mask, corners[3], Point(corners[3].x, corners[3].y + length), WHITE, thickness)

        val colouredMat = createColouredMat().setTo(colour, mask)

        return mergeMat(inputMat, colouredMat, mask)
    }

    /**
     *  This private function calculates the corners of the scanner frame
     *  @param input is by default the inputMat
     *
     *  @return the four corners of the scanner frame in a array
     */
    private fun scannerFrameCorners (input : Mat = inputMat) : Array<Point> {

        val topRight = Point(input.width()*0.3, input.height()*0.3)
        val topLeft = Point(topRight.x, input.height()-topRight.y)
        val buttomLeft = Point(topLeft.x+topLeft.y-topRight.y, topLeft.y)
        val buttomRight = Point(buttomLeft.x, topRight.y)

        return arrayOf(topRight, topLeft , buttomLeft, buttomRight)
    }

    /**
     *  This public function changes the rendered digit colour
     *  @param colour
     */
    fun setDigitColour (colour : Scalar) {

        digitColour = colour
    }

    /**
     *  This public funtion gets the value of SUDOKU_CORNER_IS_NULL
     */
    fun getSudokuCornerIsNull () : Boolean {
        return SUDOKU_CORNER_IS_NULL
    }

    /**
     *  FOR TESTING
     *
     *  This private function draws a sudoku grid on mat.
     *  @param input is by default sudoku_mask
     *  @param colour of the line
     *  @param thickness of the line
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
     *  This private function resizes a mat to another size.
     *  @param input is the mat which is to resize
     *  @param output is the output size. Default value is the camera view size.
     *
     *  @return resized mat
     */
    private fun resizeMat (input : Mat, outputSize : Size = inputSize) : Mat {

        var dst : Mat = Mat.zeros(outputSize, matType)
        Imgproc.resize(input, dst, outputSize)

        return dst
    }

    /**
     *  FOR TESTING
     *
     *  This public function starts the timer.
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

    /**
     *  FOR TESTING
     *
     *  This public function draws a circle in the center of the screen
     */
    private fun drawCircle (input : Mat = inputMat, size : Size = inputSize, colour : Scalar = digitColour, thickness: Int = 10) : Mat {

        val centerPoint : Point = Point(size.width/2, size.height/2)
        val radius : Int = size.width.toInt()/10

        var mask = Mat.zeros(input.size(), matType)

        Imgproc.circle(mask,centerPoint, radius, WHITE, thickness, LINE_AA, 0 )

        val colouredMat = createColouredMat().setTo(colour, mask)

        return mergeMat(input, colouredMat, mask)
    }
}