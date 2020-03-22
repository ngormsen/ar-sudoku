package sse.goethe.arsudoku

import org.opencv.core.*
import org.opencv.core.Core.LINE_8
import org.opencv.core.Core.inRange
import org.opencv.imgproc.Imgproc
import sse.goethe.arsudoku.ml.Recognition

/**
 * author: Kelvin Tsang
 */
class Visualisation(recognition: Recognition) {

    // colums and row size
    private val TOTAL_ROWS = 9
    private val TOTAL_COLS = 9

    private val TEXT_COLOR = Scalar(255.0, 255.0, 255.0, 0.0) // white = (255,255,255)

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
            //perspectiveTransform()
            //createMask()
            //mergeMat()
            testPlot(sudokuMat!!)
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
                val digit = digits!![row][col].toString()
                if (digit != null && digit != "0") {
                    val x = row * cellWidth.toDouble()
                    val y = col * cellWidth.toDouble()
                    Imgproc.putText(canvas,
                                    digit,
                                    Point(x,y),
                            3,
                            1.0,
                                     TEXT_COLOR,
                            1,
                                    LINE_8,
                        true)
                }
            }
        }
        sudokuMat = rotateMat(canvas)
    }

    /**
     *  Function rotateMat (input: Mat)
     *      - rotate Mat by (angle =) 90 degrees
     *
     *      https://stackoverflow.com/questions/15043152/rotate-opencv-matrix-by-90-180-270-degrees
     */
    private fun rotateMat (input : Mat) : Mat {
        val angle = 270.0 // degrees
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
     *  Function testPlot (input: Mat to plot)
     *      -  resize and plot the input
     */
    private fun testPlot (test : Mat) {
        outputMat = Mat.zeros(inputSize, inputType!!)
        Imgproc.resize(test,outputMat,inputSize)
    }
}