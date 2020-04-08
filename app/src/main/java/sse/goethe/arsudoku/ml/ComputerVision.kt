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
 * todo
 * 1. preprocessing                              Done
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
 * 5.1 With completely black picture, no contour can be found, results in null array and crash
 * 5.2 Kelvin fragen nach Kamera Auflösung
 * 6. Documentation
 * 6.1 Class diagram
 * 6.2
 *
 * 25.03.
 * Integration in MainActivity
 * Manus ding
 * davids ding
 *
 * Idea: experimentalContouring
 * crop first, then do contour detection on cropped image again to search for 81 square contours
 *
 */
class ComputerVision {

    /* values and variables */
    private lateinit var bitmap: Bitmap
    private val SINGLE_DIM_SIZE_ONE_SUDOKU_SQUARE = 28  // the width and height of one Sudoku number square
    private val CROPPEDSUDOKUSIZE = 9 * SINGLE_DIM_SIZE_ONE_SUDOKU_SQUARE
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

    /* init is called once if class is instantiated */
    init {
        /* don't forgett to init lateinit variables */
    }

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
        // ToDo: create more Sudoku viability checks

        // reset everything to null, because it's at this point not clear if there is a sudoku
        SudokuCorners = null
        CroppedSudoku = null
        TransformationMat = null
        SudokuBoxes = null
        SudokuBoxesBitmap = null

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
        var boxesRotate = boxes

        for (i in 0..80) {
            boxesRotate[i] = rotateMat(boxes[i])
        }

        // ToDo: move this code to the convertMatToBitmap function!
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

    fun lineDetection(frame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        /**
         * 1. Canny Edge Detection
         * 2. Hough Transform
         * 3. Join Lines..??
         * 4. Check for Sudoku
         * 5. Find Intersections
         */
        val (img, _) = preprocessing(frame)
        val fra = Mat.zeros(frame.gray().size(), frame.gray().type())
        val canny = Mat()
        Imgproc.Canny(img, canny, 100.0, 200.0)
        val lines = Mat()
        Imgproc.HoughLines(canny, lines, 1.0, PI/180, 200)
        for (i in 0 until lines.rows()){
            drawLine(lines.get(i,0), fra)
        }
        return fra
    }

    /* helper because man opencv cant even draw the lines it calculates
    * https://aishack.in/tutorials/sudoku-grabber-opencv-detection/
    */
    private fun drawLine(line: DoubleArray, img: Mat) {
        val color = Scalar(255.0, 0.0, 0.0)
        if(line[1]!=0.0) {
            val m = -1/tan(line[1])

            val c = line[0]/sin(line[1])

            Imgproc.line(img, Point(0.0, c), Point(img.size().width, m*img.size().width+c), color, 1)
        }
        else {
            Imgproc.line(img, Point(line[0], 0.0), Point(line[0], img.size().height), color, 1)
        }
    }

    private fun experimentalContouring(frame: Mat) {
        /**
         * https://github.com/ColinEberhardt/wasm-sudoku-solver/blob/master/src/steps/findSudokuGrid.js
         * Doesn't seem very helpful so far!
         */
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(frame, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)
        hierarchy.release()

        // approximates each contour to polygon
        val rectangles = mutableListOf<MatOfPoint2f>()
        for (contour in contours) {
            val approximatedContour = MatOfPoint2f()
            val c = MatOfPoint2f()
            contour.convertTo(c, CvType.CV_32F)
            Imgproc.approxPolyDP(c, approximatedContour, 10.0, true)

            // is it a rectangle contour?
            if (approximatedContour.size().height == 4.0) {
                rectangles.add(approximatedContour)
            }

            approximatedContour.release()
        }

        Log.d("experimentalContour", "${rectangles.size}")
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

    // ############################################################
    // New preprocessing bc of classifier performance reasons

    private fun preprocessing_v2(frame: CameraBridgeViewBase.CvCameraViewFrame): Pair<Mat, Mat> {
        var proc = Mat()
        Imgproc.GaussianBlur(frame.rgba(), proc, Size(9.0,9.0), 0.0)
        var tmp = Mat()
        Imgproc.adaptiveThreshold(proc, tmp, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2.0)
        var tmp2 = Mat()
        bitwise_not(tmp, tmp2)
        var kernel = Mat.zeros(3,3, CV_8UC1)
        var dst = Mat()
        dilate(tmp2, dst, kernel)

        return Pair(dst, tmp)
    }

    private fun scale_and_centre(){

    }


    private fun rotateMat (input : Mat, angle : Double = 270.0) : Mat {

        val centerPoint : Point = Point(input.cols()/2.0, input.rows()/2.0)
        val rotMat : Mat = Imgproc.getRotationMatrix2D(centerPoint, angle, 1.0)
        var dst : Mat = Mat.zeros(input.cols(), input.rows(), input.type())

        Imgproc.warpAffine(input, dst, rotMat, input.size(), Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT)

        return dst
    }

    // ############################################################


    /**
     * Finds the largest contour in the frame
     * and gets the four corners of it, should it be similar to a square.
     *
     */
    private fun findCorners(frame: Mat): MatOfPoint2f? {

        // We do contour detection in this function. This is the most simple and only works when
        // the Sudoku is the single largest entity on the screen. Has no viability check.

        // ToDo: Find the first 4 largest contours, check if we can make any of them a square, then rest.

        val contours = ArrayList<MatOfPoint>() // destination for findContours()

        // TODO: WHAT IF IT IS NOT POSSIBLE TO FIND A CONTOUR? -> Use try, catch
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
        val approx = MatOfPoint2f()
        val x = MatOfPoint2f()
        biggest.convertTo(approx, CvType.CV_32F)
        biggest.convertTo(x, CvType.CV_32F)

        var d = 0.0
        while(approx.toArray().size > 4) {
            d++
            Imgproc.approxPolyDP(x, approx, d,true)

            // 4.2.1: if we need too many iterations of this, it's probably not a square/sudoku
            if (d > 50) return null // ToDo: figure out a reasonable d
        }

        // this check is necessary, because we might go from >4 points to <4 in a single increment of d
        if (approx.toList().size < 4) return null

        return approx
    }


    /**
     * This function takes an Image and returns a cropped Image
     * with only the sudoku in it
     */
    private fun cropImage(image: Mat, srcCoords: MatOfPoint2f): Mat{
        // destination vertices
        val dstCoords: MatOfPoint2f = sortPointsArray(MatOfPoint2f( Point(0.0,0.0), Point(0.0, CROPPEDSUDOKUSIZE.toDouble()), Point(CROPPEDSUDOKUSIZE.toDouble(), 0.0), Point(CROPPEDSUDOKUSIZE.toDouble(), CROPPEDSUDOKUSIZE.toDouble()) ))
        // the destination buffer
        val dst = Mat.zeros(CROPPEDSUDOKUSIZE, CROPPEDSUDOKUSIZE, CV_8UC3) // TODO: not 100% sure about the type here...
        // create the perspective transform
        TransformationMat = Imgproc.getPerspectiveTransform(srcCoords, dstCoords)
        // apply to the image
        Imgproc.warpPerspective(image, dst, TransformationMat, dst.size(), INTER_LINEAR, BORDER_CONSTANT) // ToDo: is the zoom problem here, that the warp is too close up
        return dst
    }
    /**
     * IGNORE this function!
     * this funtion is similar to the above and to be used for testing purposes
     */
    private fun cropImageOn(image: Mat, srcCoords: MatOfPoint2f): Mat{
        // destination vertices
        //val dstCoords: MatOfPoint2f = sortPointsArray(MatOfPoint2f(Point((image.width()/4).toDouble(),(image.height()/4).toDouble()), Point((image.width()/4).toDouble(),(image.height()/4 + DerBreite).toDouble()), Point((image.width()/4 + DerBreite).toDouble(), (image.height()/4).toDouble()), Point((image.width()/4 + DerBreite).toDouble(), (image.height()/4 + DerBreite).toDouble())))
        val dstCoords: MatOfPoint2f = sortPointsArray(MatOfPoint2f( Point(0.0,0.0), Point(0.0, CROPPEDSUDOKUSIZE.toDouble()), Point(CROPPEDSUDOKUSIZE.toDouble(), 0.0), Point(CROPPEDSUDOKUSIZE.toDouble(), CROPPEDSUDOKUSIZE.toDouble()) ))
        // the destination buffer
        val dst = Mat.zeros(image.size(), CV_8UC3)
        // create the perspective transform
        val perspectiveTransform = Imgproc.getPerspectiveTransform(srcCoords, dstCoords)
        // apply to the image
        Imgproc.warpPerspective(image, dst, perspectiveTransform, image.size(), INTER_LINEAR, BORDER_CONSTANT)
        Log.d("Points", "first x: ${dstCoords.toList()[0].x}, first y: ${dstCoords.toList()[0].y}, second x: ${dstCoords.toList()[3].x}, second y: ${dstCoords.toList()[3].y}")
        //val r = Rect(dstCoords.toList()[0], dstCoords.toList()[3])
        //val wowi = Mat(dst, r)
        //dst = Mat.zeros(dst.size(), CV_8UC3)
        //Imgproc.resize(wowi, dst, dst.size())
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
        val squares: Array<Mat> = Array(81) { Mat.zeros(Size(SINGLE_DIM_SIZE_ONE_SUDOKU_SQUARE.toDouble(), SINGLE_DIM_SIZE_ONE_SUDOKU_SQUARE.toDouble()), sudoku.type())}

        for (row in 0..8){ // each row
            for (column in 0..8){ // each column of the current row
                // make a rectangle from the left upper and the right lower point
                val r = Rect(Point(column*SINGLE_DIM_SIZE_ONE_SUDOKU_SQUARE.toDouble(), row*SINGLE_DIM_SIZE_ONE_SUDOKU_SQUARE.toDouble()), Point((column+1)*SINGLE_DIM_SIZE_ONE_SUDOKU_SQUARE.toDouble(), (row+1)*SINGLE_DIM_SIZE_ONE_SUDOKU_SQUARE.toDouble()))
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
     * This entire function looks like cancer, because I feel like I'm still doing typing
     * in kotlin wrong...
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
     * The calculateCellCoordinates function is a private helper function
     * which determines/calculates all 81 cell mid points.
     * The midpoints will be used to render the text/digits exactly
     * on this coordinates.
     *
     * Input: Array of the 4 edge coordinates of the Sudoku
     * Output: Array of 81 cell mid points
     *
     * */
    private fun calculateCellMidCoordinates(coordArray: Array<Point>): Array<Point> {
        var cellCoordinates: Array<Point> // exactly 81
        return coordArray
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
}