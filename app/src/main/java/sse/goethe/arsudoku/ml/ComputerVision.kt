/**
 * @author Christopher Wiesner
 * date: 29.02.2020
 */
package sse.goethe.arsudoku.ml

import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.createBitmap
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.Core.BORDER_CONSTANT
import org.opencv.core.Core.bitwise_not
import org.opencv.core.CvType.*
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.*
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.tan

/**
 * The ComputerVision class ...
 *
 * Use it in MainActivity.kt. Especially in onCameraFrame()
 *
 *
 *
 * 1. preprocessing
 * 2. finding corners/intersections
 * 2.1 sudokuCellMidCoordinates (+ the current tilt of sudoku maybe?) -> do the perspective transform inversely
 * 2.2 sudokuEdgeCoordinates
 * 3. cropping
 * 3.1 croppedSudokuBlocks: Array<Bitmap>
 * 4. Miscellaneous
 * 4.1 convertMatToBitmap
 * 4.2 viability check
 * 4.2.1 look at how many approxDP invokations are usually needed to approximate the
 * sudoku contour to 4 points. If the currently needed number greatly exceeds this,
 * it's probably not a sudoku.
 * 5. Known Bugs
 * 6. Documentation
 * 6.1 Class diagram
 * 6.2
 *
 *
 */
class ComputerVision {

    /* values and variables */
    private val SINGLE_DIM_SIZE_ONE_SUDOKU_SQUARE = 28.0  // the width and height of one Sudoku number square
    private val CROPPEDSUDOKUSIZE = 9 * SINGLE_DIM_SIZE_ONE_SUDOKU_SQUARE
    private val CROPPEDSUDOKUSIZE_2D = Size(CROPPEDSUDOKUSIZE,CROPPEDSUDOKUSIZE)

    /**
     * The following are class properties that are being set by analyzeFrame().
     * They are nullable. You MUST check for null value. If null value is found, that
     * indicates, that no Sudoku was found in the frame.
     */
    var SudokuCorners: MatOfPoint2f? = null
    var CroppedSudoku: Mat? = null
    var TransformationMat: Mat? = null
    var SudokuBoxes: Array<Mat>? = null
    var SudokuBoxesBitmap: Array<Bitmap>? = null

    private lateinit var SCANNERFRAME_CORNERS : MatOfPoint2f


    /**###############################################################
     * class functions
     *################################################################*/


    /**
     * The overall funciton that is to be called from outside the class
     * It will set some class-internal attributes, that can then be called
     * outside of the class.
     *
     * Call this once for every frame. The class attributes are:
     * SudokuCorners
     * CroppedSudoku
     * TransformationMat
     * SudokuBoxes
     *
     * All of them are nullable, so you MUST check for null value.
     * If any null value is found, no Sudoku was found
     * in the frame
     *
     * */
    fun analyzeFrame( frame: CameraBridgeViewBase.CvCameraViewFrame ) {

        // reset everything to null, because it's at this point not clear if there is a sudoku
        SudokuCorners = null
        CroppedSudoku = null
        TransformationMat = null
        SudokuBoxes = null
        SudokuBoxesBitmap = null

        SCANNERFRAME_CORNERS =scannerFrameCorners(frame.gray())

        // Preprocessing:
        val (img, forML) = preprocessing(frame)

        // Finding Corners using Contour Detection:
        var corners = findCorners(img)

        // experimental box finding:
        // experimentalContouring(img)

        // corners is a nullable MatOfPoint2f
        // if null, there was no Sudoku in the frame
        if (corners == null){
            return
        }else{
            corners = sortPointsArray(corners)
        }

        // cropping
        val croppedImage: Mat
        croppedImage = cropImage(forML, corners)

        // cutting
        val boxes = cutSudoku(croppedImage)
        val boxesRotate = boxes

        for (i in 0..80) {
            boxesRotate[i] = rotateMat(boxes[i])
        }

        // convert Mat images to Bitmaps
        val boxesBitmap: Array<Bitmap> = Array(81) {
            createBitmap( boxesRotate[0].width(), boxesRotate[0].height() )
        }
        for (i in 0..80) {
            boxesBitmap[i] = convertMatToBitmap(boxesRotate[i])
        }

        // In the end, we set all the calculated data as class properties
        SudokuCorners = corners
        CroppedSudoku = croppedImage
        // TransformationMat = ... // setting of this var is handled in cropImage()
        SudokuBoxes = boxes
        SudokuBoxesBitmap = boxesBitmap

    }


    /**
     *  Image Preprocessing:
     *
     *  Greyscale
     *  Gaussian Blur
     *  Adaptive Thresolding
     *  Open
     *  Dilate
     *  */
    private fun preprocessing(frame: CameraBridgeViewBase.CvCameraViewFrame): Pair<Mat, Mat>{
        val grayMat: Mat = frame.gray()
        val blurMat = Mat()
        Imgproc.GaussianBlur(grayMat, blurMat, Size(9.0,9.0), 0.0)
        val threshMat = Mat()
        Imgproc.adaptiveThreshold(blurMat, threshMat, 255.0,1,1,11,2.0)
        val morphedMat = Mat()
        val core = Imgproc.getStructuringElement(0, Size(2.0, 2.0))
        Imgproc.morphologyEx(threshMat, morphedMat, 2, core)
        val dilatedMat = Mat()
        Imgproc.dilate(morphedMat, dilatedMat, core)
        return Pair(dilatedMat, threshMat)
    }


    private fun rotateMat (input : Mat, angle : Double = 270.0) : Mat {

        val centerPoint : Point = Point(input.cols()/2.0, input.rows()/2.0)
        val rotMat : Mat = Imgproc.getRotationMatrix2D(centerPoint, angle, 1.0)
        val dst : Mat = Mat.zeros(input.cols(), input.rows(), input.type())

        Imgproc.warpAffine(input, dst, rotMat, input.size(), Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT)

        return dst
    }



    /**
     * Finds the largest contour in the frame
     * and gets the four corners of it, should it be similar to a square.
     *
     */
    private fun findCorners(input : Mat): MatOfPoint2f? {

        /**
         *  Crop the input mat
         */
        var roi = Rect(SCANNERFRAME_CORNERS.toList()[0], SCANNERFRAME_CORNERS.toList()[2])
        var frame = Mat(input, roi)
        /**
         *  ========================================================================================
         */

        // We do contour detection in this function.

        val contours = ArrayList<MatOfPoint>() // destination for findContours()

        val hierarchy = Mat()
        Imgproc.findContours(frame, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)
        hierarchy.release()

        var biggest = MatOfPoint()
        var max_area = 0.0

        for (contour in contours) {
            if (Imgproc.contourArea(contour) > max_area) {
                biggest = contour
                max_area = Imgproc.contourArea(contour)
            }
        }

        // if the contour can be approximated by 3 or fewer points, it's clearly not a square and
        // therefore clearly not a sudoku! We return null.
        if (biggest.toList().size < 4) return null

        // convert to MatOfPoint2f from MatOfPoint (which was the result of findContours. But approxDP needs 2f)
        var approx = MatOfPoint2f()
        val x = MatOfPoint2f()
        biggest.convertTo(approx, CvType.CV_32F)
        biggest.convertTo(x, CvType.CV_32F)

        var d = 0.0
        while(approx.toArray().size > 4) {
            d++
            Imgproc.approxPolyDP(x, approx, d,true)

            // 4.2.1: if we need too many iterations of this, it's probably not a square/sudoku
            if (d > 50) return null // d = 50 seems to be a reasonable number
        }

        // this check is necessary, because we might go from >4 points to <4 in a single increment of d
        if (approx.toList().size < 4) return null

        /**
         *  Adding...
         */
        val topRight = SCANNERFRAME_CORNERS.toArray()[0]
        approx = MatOfPoint2f(  Point(approx.toArray()[0].x + topRight.x, approx.toArray()[0].y + topRight.y),
                                Point(approx.toArray()[1].x + topRight.x, approx.toArray()[1].y + topRight.y),
                                Point(approx.toArray()[2].x + topRight.x, approx.toArray()[2].y + topRight.y),
                                Point(approx.toArray()[3].x + topRight.x, approx.toArray()[3].y + topRight.y))
        /**
         *  ========================================================================================
         */

        return approx
    }


    /**
     * This function takes an Image and returns a cropped Image
     * with only the sudoku in it
     */
    private fun cropImage(image: Mat, srcCoords: MatOfPoint2f): Mat{
        // destination vertices
        val dstCoords: MatOfPoint2f = MatOfPoint2f( Point(0.0,0.0), Point(CROPPEDSUDOKUSIZE, 0.0), Point(0.0, CROPPEDSUDOKUSIZE), Point(CROPPEDSUDOKUSIZE, CROPPEDSUDOKUSIZE) )
        // the destination buffer
        val dst = Mat.zeros(CROPPEDSUDOKUSIZE_2D, CV_8UC3)
        // create the perspective transform
        TransformationMat = Imgproc.getPerspectiveTransform(srcCoords, dstCoords)
        // apply to the image
        Imgproc.warpPerspective(image, dst, TransformationMat, dst.size(), INTER_LINEAR, BORDER_CONSTANT)
        return dst
    }

    /**
     * Takes the image of a sudoku and cuts it into 81 sub-images,
     * each containing the image of a single digit
     *
     * The returned Array contains the Mats of each box.
     * They are ordered row by row, e.g. squares[17] would be row 2 column 8
     *
     */
    private fun cutSudoku(sudoku: Mat): Array<Mat>{
        val squares: Array<Mat> = Array(81) { Mat.zeros(Size(SINGLE_DIM_SIZE_ONE_SUDOKU_SQUARE, SINGLE_DIM_SIZE_ONE_SUDOKU_SQUARE), sudoku.type())}

        for (row in 0..8){ // each row
            for (column in 0..8){ // each column of the current row
                // make a rectangle from the left upper and the right lower point
                val r = Rect(Point(column*SINGLE_DIM_SIZE_ONE_SUDOKU_SQUARE, row*SINGLE_DIM_SIZE_ONE_SUDOKU_SQUARE), Point((column+1)*SINGLE_DIM_SIZE_ONE_SUDOKU_SQUARE, (row+1)*SINGLE_DIM_SIZE_ONE_SUDOKU_SQUARE))
                // use rect to cut out roi from sudoku
                val oneSquare = Mat(sudoku, r)
                squares[row*9+column] = oneSquare
            }
        }
        return squares
    }

    /**
     * The sortPointsArray function is a private helper function
     * which sorts an Array of OpenCV "Point"s in the following order.
     *
     * First coordinate: Top left
     * Second coordinate: Top right
     * Third coordinate: Bottom left
     * Fourth coordinate Bottom right
     *
     * Input: MatOfPoint2f
     * Output: MatOfPoint2f // sorted
     *
     * */
    private fun sortPointsArray(coordMat: MatOfPoint2f): MatOfPoint2f {
        val coordList = coordMat.toList()
        Log.d("points", "${coordList.size}")

        // 1. sort by y-pos
        coordList.sortBy{it.y}

        // 2. split into upper and lower half
        val firstHalf = coordList.slice(0..1).toMutableList()
        val secondHalf = coordList.slice(2..3).toMutableList()

        // 3. sort each half by x-pos
        firstHalf.sortBy{it.x}
        secondHalf.sortBy{it.x}

        // 4. merge back together for final result
        val r: MatOfPoint2f = MatOfPoint2f()
        r.fromList(firstHalf + secondHalf)
        return r
    }

    /**
     * The convertMatToBitmap function is a private helper function
     * which converts a frame as Mat datatype to a Bitmap.
     * This is used to create the Bitmap for the classifier.
     *
     * Input: frame as Mat
     * Output: converted Mat as Bitmap
     *
     * Use this function in analyzeFrame()
     * */
    private fun convertMatToBitmap(frameMat: Mat): Bitmap {
        val bmp: Bitmap = Bitmap.createBitmap(frameMat.cols(), frameMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(frameMat, bmp)
        return bmp
    }

    var START_DIGIT_CLASSIFIER = true // if true, Digit Classifier can start

    /**
     *  This public funtion set the var DIGIT_CLASSIFIER to true/false.
     */
    fun setStartDigitClassifier(bool : Boolean) {
        START_DIGIT_CLASSIFIER = bool
    }

    /**
     *  This public funtion get the value of DIGIT_CLASSIFIER.
     */
    fun getStartDigitClassifier() : Boolean {
        return START_DIGIT_CLASSIFIER
    }

    /**
     *  This private function checks if there a sudoku corners.
     *  If there are not sudoku corner, the next time if it found sudoku corners it start the digit recognition.
     */
     fun checkCorners() {
        if (!START_DIGIT_CLASSIFIER) {
            START_DIGIT_CLASSIFIER = SudokuCorners == null
        }
    }

    /**
     *  This private function calculates the corners of the scanner frame
     *  @param input is by default the inputMat
     *
     *  @return the four corners of the scanner frame in a array
     */
    private fun scannerFrameCorners (input : Mat) : MatOfPoint2f {

        val topRight = Point(input.width()*0.25, input.height()*0.25)
        val topLeft = Point(topRight.x, input.height()-topRight.y)
        val buttomLeft = Point(topLeft.x+topLeft.y-topRight.y, topLeft.y)
        val buttomRight = Point(buttomLeft.x, topRight.y)

        val length = (topLeft.y-topRight.y).toInt().toString()

        return MatOfPoint2f(topRight, topLeft , buttomLeft, buttomRight)
    }
}