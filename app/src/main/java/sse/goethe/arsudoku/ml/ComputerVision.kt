/**
 * @author Christopher Wiesner
 * date: 29.02.2020
 */
package sse.goethe.arsudoku.ml

import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.Core.BORDER_CONSTANT
import org.opencv.core.CvType.CV_8UC3
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.INTER_LINEAR
import kotlin.math.PI
import kotlin.math.abs
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
 *
 *
 */
class ComputerVision {

    /* values and variables */
    private lateinit var bitmap: Bitmap
    private val SINGLE_DIM_SIZE_ONE_SUDOKU_SQUARE = 32  // the width and height of one Sudoku number square
    private val CROPPEDSUDOKUSIZE = 9 * SINGLE_DIM_SIZE_ONE_SUDOKU_SQUARE
    var SudokuCorners: MatOfPoint2f? = null
    var CroppedSudoku: Mat? = null
    var TransformationMat: Mat? = null
    var SudokuBoxes: Array<Mat>? = null

    /* init is called once if class is instantiated */
    init {
        /* don't forgett to init lateinit variables */
    }

    /**###############################################################
     * class functions
     *################################################################*/

    public fun lineDetection(frame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        /**
         * 1. Canny Edge Detection
         * 2. Hough Transform
         * 3. Join Lines..??
         * 4. Check for Sudoku
         * 5. Find Intersections
         */
        val img = preprocessing(frame)
        val fra = Mat.zeros(frame.gray().size(), frame.gray().type())
        val canny: Mat = Mat()
        Imgproc.Canny(img, canny, 100.0, 200.0)
        val lines: Mat = Mat()
        Imgproc.HoughLines(canny, lines, 1.0, PI/180, 200)
        for (i in 0..lines.rows()-1){
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
            val m = -1/tan(line[1]);

            val c = line[0]/sin(line[1]);

            Imgproc.line(img, Point(0.0, c), Point(img.size().width, m*img.size().width+c), color, 1)
        }
        else {
            Imgproc.line(img, Point(line[0], 0.0), Point(line[0], img.size().height), color, 1)
        }
    }

    /**
     * Finds the larges contour in the frame
     * and gets the four corners of it, should it be similar to a square
     *
     */
    public fun findCorners(frame: Mat): MatOfPoint2f? {

        // We do contour detection in this function. This is the most simple and only works when
        // the Sudoku is the single largest entity on the screen. Has no viability check.

        val contours = ArrayList<MatOfPoint>() // destination for findContours()

        // TODO: WHAT IF IT IS NOT POSSIBLE TO FIND A CONTOUR? -> Use try, catch
        val hierarchy: Mat = Mat()
        Imgproc.findContours(frame, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)
        hierarchy.release()

        var biggest: MatOfPoint = MatOfPoint()
        var max_area = 0.0

        for (contour in contours){
            if (Imgproc.contourArea(contour) > max_area){
                biggest = contour
                max_area = Imgproc.contourArea(contour)
            }
        }

        // if the contour can be approximated by 3 or fewer points, it's clearly not a square and
        // therefore clearly not a sudoku! We return null.
        if (biggest.toList().size < 4) return null

        val approx: MatOfPoint2f = MatOfPoint2f() // TODO: DIES NOCHMAL CHECKEN
        approx.fromList(biggest.toList())
        var d = 0.0
        while(approx.toArray().size > 4) {
            d++
            val x: MatOfPoint2f = MatOfPoint2f()
            x.fromList(biggest.toList())
            Imgproc.approxPolyDP(x, approx, d,true)

            // 4.2.1: if we need too many iterations of this, it's probably not a square/sudoku
            if (d > 100) return null // ToDo: figure out a reasonable d
        }



        return approx
    }

    /* Image Preprocessing */
    private fun preprocessing(frame: CameraBridgeViewBase.CvCameraViewFrame): Mat{

        val grayMat: Mat = frame.gray()
        val blurMat: Mat = Mat()
        Imgproc.GaussianBlur(grayMat, blurMat, Size(9.0,9.0), 0.0)
        val threshMat: Mat = Mat()
        Imgproc.adaptiveThreshold(blurMat, threshMat, 255.0,1,1,11,2.0)
        val morphedMat: Mat = Mat()
        val core = Imgproc.getStructuringElement(0, Size(2.0, 2.0))
        Imgproc.morphologyEx(threshMat, morphedMat, 2, core)
        val dilatedMat: Mat = Mat()
        Imgproc.dilate(morphedMat, dilatedMat, core)
        return dilatedMat
    }

    /**
     * The overall funciton that is to be called from outside the class
     * It will set some class-internal attributes, that can then be called
     * outside of the class.
     *
     * */
    fun analyzeFrame( frame: CameraBridgeViewBase.CvCameraViewFrame ) {
        // ToDo: create more Sudoku viability checks

        // reset everything to null, because it's at this point not clear if there is a sudoku
        SudokuCorners = null
        CroppedSudoku = null
        TransformationMat = null
        SudokuBoxes = null

        // Preprocessing:
        val img = preprocessing(frame)

        // Finding Corners using Contour Detection:
        var corners = findCorners(img)

        // corners is a nullable MatOfPoint2f
        // if null, there was no SUdoku in the frame
        if (corners == null){
            return
        }else{
            corners = sortPointsArray(corners)
        }

        val croppedImage: Mat
        croppedImage = cropImage(frame.gray(), corners)
        val boxes = cutSudoku(croppedImage)

        // In the end, we set as class properties all the calculated data
        SudokuCorners = corners
        CroppedSudoku = croppedImage
        // TransformationMat = ... // setting of this var is handled in cropImage()
        SudokuBoxes = boxes
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
        Imgproc.warpPerspective(image, dst, TransformationMat, dst.size(), INTER_LINEAR, BORDER_CONSTANT)
        return dst
    }
    /**
     * this funtion is similar to the above and to be used for testing purposes
     */
    private fun cropImageOn(image: Mat, srcCoords: MatOfPoint2f): Mat{
        // destination vertices
        //val dstCoords: MatOfPoint2f = sortPointsArray(MatOfPoint2f(Point((image.width()/4).toDouble(),(image.height()/4).toDouble()), Point((image.width()/4).toDouble(),(image.height()/4 + DerBreite).toDouble()), Point((image.width()/4 + DerBreite).toDouble(), (image.height()/4).toDouble()), Point((image.width()/4 + DerBreite).toDouble(), (image.height()/4 + DerBreite).toDouble())))
        val dstCoords: MatOfPoint2f = sortPointsArray(MatOfPoint2f( Point(0.0,0.0), Point(0.0, CROPPEDSUDOKUSIZE.toDouble()), Point(CROPPEDSUDOKUSIZE.toDouble(), 0.0), Point(CROPPEDSUDOKUSIZE.toDouble(), CROPPEDSUDOKUSIZE.toDouble()) ))
        // the destination buffer
        var dst = Mat.zeros(image.size(), CV_8UC3);
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
        val xPoints = Array(9) { i -> i * CROPPEDSUDOKUSIZE}
        val yPoints = Array(9) { i -> i * CROPPEDSUDOKUSIZE}
        val squares: Array<Mat> = Array(81) { Mat.zeros(Size(CROPPEDSUDOKUSIZE.toDouble(), CROPPEDSUDOKUSIZE.toDouble()), sudoku.type())}
        for (row in 0..9){ // each row
            for (column in 0..9){ // each column of the current row
                // make a rectangle from the left upper and the right lower point
                val r = Rect(Point(column.toDouble(), row.toDouble()), Point(column+1.toDouble(), row+1.toDouble()))
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
        var coordList = coordMat.toList()

        // 1. sort by y-pos
        coordList.sortBy{it.y}

        // 2. split into upper and lower half
        var firstHalf = coordList.slice(0..1).toMutableList()
        var secondHalf = coordList.slice(2..3).toMutableList()

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
        var bmp: Bitmap = Bitmap.createBitmap(frameMat.cols(), frameMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(frameMat, bmp)
        return bmp
    }

    /**
     * The cropSudoku function is a private helper function
     * which takes a Bitmap of a Sudoku and crops it into
     * 81 pieces to get each Sudoku cell.
     *
     * Input: Bitmap of a Sudoku
     * Output: Array pf 81 Bitmap's
     *
     * Use this after launching convertMatToBitmap() to create
     * the Bitmap Array.
     **/
    fun cropSudoku(sudokuImg: Bitmap):Array<Bitmap> {
        return Array(81) { sudokuImg }
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