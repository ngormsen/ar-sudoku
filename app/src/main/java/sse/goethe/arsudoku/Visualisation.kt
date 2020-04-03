package sse.goethe.arsudoku

import android.content.ContentValues.TAG
import android.graphics.Color
import android.util.Log
import org.opencv.android.CameraBridgeViewBase
import org.opencv.core.*
import org.opencv.core.Core.*
import org.opencv.imgproc.Imgproc
import sse.goethe.arsudoku.ml.Recognition

/**
 *      author: Kelvin Tsang
 *
 *      Class Visualisation
 *          - Rendering digits in to the sudoku
 */
class Visualisation(recognition: Recognition) {

    // colums and row size
    private val TOTAL_ROWS = 9
    private val TOTAL_COLS = 9

    // font attributes
    private val FONT_COLOR_BLACK = Scalar(0.0, 0.0, 0.0, 0.0)
    private val FONT_COLOR_WHITE = Scalar(255.0, 255.0, 255.0, 0.0)
    private val FONT_FACE = FONT_HERSHEY_DUPLEX // https://codeyarns.github.io/tech/2015-03-11-fonts-in-opencv.html
    private val FONT_SCALE = 0.6
    private val FONT_THICKNESS = 1
    private val FONT_LINETYPE = LINE_AA //antialiased line

    private val ROTANTION_ANGLE = 90.0
    
    // colour Scalar(Blue, Green, Red)
    private val BLACK = Scalar(0.0, 0.0,0.0)
    private val WHITE = Scalar(255.0, 255.0,255.0)
    private val BLUE = Scalar(255.0, 0.0,0.0)
    private val GREEN = Scalar(0.0, 255.0,0.0)
    private val RED = Scalar(0.0, 0.0,255.0)
    
    // connection to Recognition and ComputerVision
    private var recognition: Recognition = recognition

    private lateinit var inputMat : Mat
    private lateinit var inputMat_rgba : Mat
    //private lateinit var inputMat_gray : Mat

    private var sudokuMat : Mat? = null
    private var transformMat : Mat? = null
    private var digits : Array<Array<Int>>? = null

    private var inputSize : Size? = null
    private var inputType : Int? = null

    private lateinit var sudoku_mask : Mat
    private lateinit var outputMat : Mat



    /**
     *   Function startVisualisation (input is the current video stream as Mat)
     *       - starts the visualisation if the sudoku is found and the input isn't null
     *       -> return outputMat as Mat
     */
    fun runVisualisation(inputFrame: CameraBridgeViewBase.CvCameraViewFrame) : Mat {

        return if (getInput(inputFrame)) {
            createSudokuMask()
            createOutput()
            mergeMat()
            //outputMat
            inputMat
        }
        else inputFrame.rgba()
    }

    /**
     *   Function getInput
     *       - get the current video stream as Mat
     *       - get the cropped and square sudoku as Mat
     *       - get the invers transformation matrix
     *       - get the sudoku digits
     */
    private fun getInput(inputFrame: CameraBridgeViewBase.CvCameraViewFrame) : Boolean {
        return if (recognition.computerVision.CroppedSudoku != null && recognition.computerVision.TransformationMat != null && recognition.sudokuPredictedDigits != null) {
            inputMat_rgba = inputFrame.rgba()
            //inputMat_rgba = inputFrame.gray()
            inputMat = inputMat_rgba
            inputSize = inputMat_rgba.size()
            inputType = inputMat_rgba.type()

            sudoku_mask = Mat.zeros(inputSize, inputType!!)
            outputMat = Mat.zeros(inputSize, inputType!!)

            sudokuMat = recognition.computerVision.CroppedSudoku
            transformMat = recognition.computerVision.TransformationMat!!.inv()
            digits = recognition.sudokuPredictedDigits

            return true
        } else false
    }

    /**
     *  Function createSudokuMask
     *      - renderDigits()
     *      - transformPerspective(...)
     *      
     *      https://stackoverflow.com/questions/45131216/opencv-overlay-two-mat-drawings-not-images-with-transparency
     */
    private fun createSudokuMask () {
        sudoku_mask = renderDigits()
        sudoku_mask = transformPerspective()
    }

    /**
     *   Function renderDigits
     *       - creates a Mat with the size of the cropped sudoku on a black background
     *       - renders the solved digits on their location in white colour
     */
    private fun renderDigits (sudoku : Mat = sudokuMat!!) : Mat{

        val matSize = sudoku.size()
        val matType = sudoku.type()
        var canvas = Mat.zeros(matSize, inputType!!)

        val cellWidth = sudoku.width() / 9

        for (row in 0 until TOTAL_ROWS) {
            for (col in 0 until TOTAL_COLS) {
                val digit = digits!![row][col].toString()
                //if (digit != null && digit != "0") {
                if (digit != null) {
                    val x = col * cellWidth.toDouble() + cellWidth*0.3
                    val y = (row+1) * cellWidth.toDouble() - cellWidth*0.3
                    Imgproc.putText(canvas,
                                    digit,
                                    Point(x,y),
                                    FONT_FACE,
                                    FONT_SCALE,
                                    FONT_COLOR_WHITE,
                                    FONT_THICKNESS,
                                    FONT_LINETYPE)
                }
            }
        }
        //canvas = drawGrid(canvas) // for testing
        return rotateMat(canvas)
    }

    /**
     *  Function rotateMat (input: Mat)
     *      - rotate Mat by 90 degrees
     *
     *      https://stackoverflow.com/questions/15043152/rotate-opencv-matrix-by-90-180-270-degrees
     */
    private fun rotateMat (input : Mat, angle : Double = ROTANTION_ANGLE) : Mat {

        val centerPoint : Point = Point(input.cols()/2.0, input.rows()/2.0)
        val rotMat : Mat = Imgproc.getRotationMatrix2D(centerPoint, angle, 1.0)
        var dst : Mat = Mat.zeros(input.cols(), input.rows(), input.type())

        Imgproc.warpAffine(input, dst, rotMat, input.size(), Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT)

        return dst
    }

    /**
     *   Function mergeMat
     *       - merged the input video stream with the edit sudoku with help of a mask
     */
    private fun mergeMat (input : Mat = inputMat, mask : Mat = sudoku_mask) {

        val dst : Mat = input
        outputMat.copyTo(dst, mask)
        outputMat = dst
    }

    /**
     *  Function perspectiveTransform
     *      - does a perspective transformation of the sudoku with help a the invers of the transform matrix
     */
    private fun transformPerspective (input : Mat = sudoku_mask) : Mat{

        var dst : Mat = Mat.zeros(inputSize, inputType!!)
        Imgproc.warpPerspective(input, dst, transformMat, inputSize, Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT)

        return dst
    }

    /**
     *  Function createOutput
     *      - creates output
     */
    private fun createOutput () {

        val whiteMat : Mat = createColouredMat()
        subtract(whiteMat, sudoku_mask, outputMat)
    }

    /** TODO: DOESN'T WORK!!! ONLY WITH WHITE COLOUR
     *  Function createColouredMat(input colour : default white)
     *      - creates a coulored Mat with the size of the input frame
     */
    private fun createColouredMat (colour : Scalar = WHITE) : Mat {

        val dst : Mat = Mat.zeros(inputSize, inputType!!)

        return dst.setTo(colour)
    }

    /**
     *  FOR TESTING
     *
     *  Function drawGrid()
     *      - draws a sudoku grid on Mat
     */
    private fun drawGrid(input : Mat = sudoku_mask, color : Scalar = WHITE, thickness : Int = 2) : Mat {

        val height = input.height().toDouble()-1
        val width = input.width().toDouble()-1
        val cellHeight = (input.height()/9).toDouble()
        val cellWidth = (input.height()/9).toDouble()

        var output : Mat = input

        // draw frame
        Imgproc.rectangle(output, Point(0.0, 0.0), Point(width, height), color, thickness, LINE_AA)
        // draw inner grid
        for (i in 1 until TOTAL_ROWS) {
            val y = i * cellHeight
            val x = i * cellWidth
            Imgproc.line(output, Point(0.0, y), Point(width, y), color, thickness, LINE_AA)
            Imgproc.line(output, Point(x, 0.0), Point(x, height), color, thickness, LINE_AA)
        }

        return output
    }

    /**
     *  FOR TESTING
     *
     *  Function resizeMat (input: Mat to resize, output: output Mat size)
     *      - resize input to output size
     */
    private fun resizeMat (input : Mat, output : Mat) : Mat {

        var dst : Mat = Mat.zeros(output.size(), output.type())
        Imgproc.resize(input, dst, output.size())

        return dst
    }
}