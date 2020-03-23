package sse.goethe.arsudoku

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
    private val FONT_COLOR = Scalar(255.0, 255.0, 255.0, 0.0) // black = (0,0,0); white = (255,255,255)
    private val FONT_FACE = FONT_HERSHEY_DUPLEX // https://codeyarns.github.io/tech/2015-03-11-fonts-in-opencv.html
    private val FONT_SCALE = 1.0
    private val FONT_THICKNESS = 1
    private val FONT_LINETYPE = LINE_AA //antialiased line

    private val ROTANTION_ANGLE = 90.0

    // connection to Recognition and ComputerVision
    private var recognition: Recognition = recognition

    private var inputMat : Mat? = null
    private var sudokuMat : Mat? = null
    private var transformMat : Mat? = null
    private var digits : Array<Array<Int>>? = null

    private var inputSize : Size? = null
    private var inputType : Int? = null

    private lateinit var mask : Mat
    private lateinit var outputMat : Mat


    /**
     *   Function startVisualisation (input is the current video stream as Mat)
     *       - starts the visualisation if the sudoku is found and the input isn't null
     *       -> return outputMat as Mat
     */
    fun runVisualisation(inputFrame: Mat) : Mat {

        return if (getInput(inputFrame)) {
            renderDigits()
            perspectiveTransform()
            //createMask()
            //mergeMat()
            resizeMat(outputMat)
            outputMat
        }
        else inputFrame
    }

    /**
     *   Function getInput
     *       - get the current video stream as Mat
     *       - get the cropped and square sudoku as Mat
     *       - get the invers transformation matrix
     *       - get the sudoku digits
     */
    private fun getInput(inputFrame: Mat) : Boolean {
        return if (recognition.computerVision.CroppedSudoku != null && recognition.computerVision.TransformationMat != null && recognition.sudokuPredictedDigits != null) {
            inputMat = inputFrame
            inputSize = inputFrame.size()
            inputType = inputFrame.type()
            sudokuMat = recognition.computerVision.CroppedSudoku
            transformMat = recognition.computerVision.TransformationMat!!.inv()
            digits = recognition.sudokuPredictedDigits

            return true
        } else false
    }

    /**
     *   Function renderDigits
     *       - creates a Mat with the size of the cropped sudoku on a black background
     *       - renders the solved digits on their location
     */
    private fun renderDigits () {

        val sudoku_matSize = sudokuMat!!.size()
        val sudoku_matType = sudokuMat!!.type()
        var canvas = Mat.zeros(sudoku_matSize, sudoku_matType)

        val cellWidth = sudokuMat!!.width() / 9

        for (row in 0 until TOTAL_ROWS) {
            for (col in 0 until TOTAL_COLS) {
                val digit = digits!![col][row].toString()
                if (digit != null && digit != "0") {
                    val x = row * cellWidth.toDouble()
                    val y = col * cellWidth.toDouble()
                    Imgproc.putText(canvas,
                                    digit,
                                    Point(x,y),
                                    FONT_FACE,
                                    FONT_SCALE,
                                    FONT_COLOR,
                                    FONT_THICKNESS,
                                    FONT_LINETYPE)
                }
            }
        }
        sudokuMat = rotateMat(canvas)
    }

    /**
     *  Function rotateMat (input: Mat)
     *      - rotate Mat by 90 degrees
     *
     *      https://stackoverflow.com/questions/15043152/rotate-opencv-matrix-by-90-180-270-degrees
     */
    private fun rotateMat (input : Mat) : Mat {
        val centerPoint : Point = Point(input.cols()/2.0, input.rows()/2.0)
        val rotMat : Mat = Imgproc.getRotationMatrix2D(centerPoint, ROTANTION_ANGLE, 1.0)
        var dst : Mat = Mat.zeros(input.cols(), input.rows(), input.type())
        Imgproc.warpAffine(input, dst, rotMat, input.size(), Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT)
        return dst
    }

    /**
     *   Function mergeMat
     *       - merged the input video stream with the edit sudoku with help of a mask
     */
    private fun mergeMat () {

        outputMat.copyTo(inputMat, mask)
        outputMat = inputMat!!
    }

    /**
     *  Function perspectiveTransform
     *      - does a perspective transformation of the sudoku with help a the invers of the transform matrix
     */
    private fun perspectiveTransform () {

        outputMat = Mat.zeros(inputSize, inputType!!)

        Imgproc.warpPerspective(sudokuMat, outputMat, transformMat, inputSize, Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT)
    }


    /**
     *  Function createMask
     *      - creates mask
     *
     *      https://stackoverflow.com/questions/45131216/opencv-overlay-two-mat-drawings-not-images-with-transparency
     */
    private fun createMask () {

        mask = sudokuMat!!
        inRange(sudokuMat,Scalar(0.0,0.0,0.0,0.0), Scalar(0.0,0.0,0.0,0.0), mask)
    }

    /**
     *  FOR TESTING
     *
     *  Function resizeMat (input: Mat to resize)
     *      -  resize input to frame size
     */
    private fun resizeMat (test : Mat) {
        outputMat = Mat.zeros(inputSize, inputType!!)
        Imgproc.resize(test,outputMat,inputSize)
    }
}