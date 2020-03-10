/**
 * @author Christopher Wiesner
 * and David Machajewski
 * date: 29.02.2020
 */
package sse.goethe.arsudoku.ml

import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
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
 * 2.1 sudokuCellMidCoordinates
 * 2.2 sudokuEdgeCoordinates
 * 3. cropping
 * 3.1 croppedSudokuBlocks: Array<Bitmap>
 * 4. Miscellaneous
 * 4.1 convertMatToBitmap
 * 4.2 viability check
 * 5. Known Bugs
 * 5.1 With completely black picture, no contour can be found, results in null array and crash
 *
 */
class ComputerVision {

    /* values and variables */
    private lateinit var bitmap: Bitmap

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
        var fra = Mat.zeros(frame.gray().size(), frame.gray().type())
        var canny: Mat = Mat()
        Imgproc.Canny(img, canny, 100.0, 200.0)
        var lines: Mat = Mat()
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

    /* find largest contour in the frame*/
    public fun contourDetection(frame: CameraBridgeViewBase.CvCameraViewFrame): Mat {

        // We do contour detection in this function. This is the most simple and only works when
        // the Sudoku is the single largest entity on the screen. Has no viability check.

        // OUTER SQUARE NO MATTER IF ITS A SUDOKU OR OTHER TYPE OF Shape
        // CAN BE RECOGNIZED NOW.
        // TODO: IMPLEMENT A HEURISTIC TO DETERMINE IF IT IS A SUDOKU OR NOT
        // WHAT IS HAPPENING IF THERE IS NOT A SUDOKU WITHIN SCREEN


        // Preprocessing:

        val img = preprocessing(frame)

        var contours = ArrayList<MatOfPoint>() // destination for findContours()

        // TODO: WHAT IF IT IS NOT POSSIBLE TO FIND A CONTOUR? -> Use try, catch
        var hierarchy: Mat = Mat()
        Imgproc.findContours(img, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)
        hierarchy.release()

        var biggest: MatOfPoint = MatOfPoint()
        var max_area = 0.0

        for (contour in contours){
            if (Imgproc.contourArea(contour) > max_area){
                biggest = contour
                max_area = Imgproc.contourArea(contour)
            }
        }

        var displayMat = frame.gray()
        Imgproc.drawContours(displayMat, mutableListOf(biggest), 0, Scalar(0.0, 255.0, 0.0), 2)

        var approx: MatOfPoint2f = MatOfPoint2f() // TODO: DIES NOCHMAL CHECKEN
        approx.fromList(biggest.toList())
        var d = 0.0
        while(approx.toArray().size>4) {
            d++
            var x: MatOfPoint2f = MatOfPoint2f()
            x.fromList(biggest.toList())
            Imgproc.approxPolyDP(x, approx, d,true);
            Log.d("approxPolyDP", "${approx.toArray().size}, $d") // TODO: remove this line
        }


        for(point in approx.toArray()){
            Imgproc.circle(displayMat, point, 50, Scalar(255.0, 0.0, 0.0), 50)
        }
        return displayMat
    }

    /* Image Preprocessing */
    private fun preprocessing(frame: CameraBridgeViewBase.CvCameraViewFrame): Mat{

        var grayMat: Mat = frame.gray()
        var blurMat: Mat = Mat()
        Imgproc.GaussianBlur(grayMat, blurMat, Size(9.0,9.0), 0.0)
        var threshMat: Mat = Mat()
        Imgproc.adaptiveThreshold(blurMat, threshMat, 255.0,1,1,11,2.0)
        var morphedMat: Mat = Mat()
        val core = Imgproc.getStructuringElement(0, Size(2.0, 2.0))
        Imgproc.morphologyEx(threshMat, morphedMat, 2, core)
        var dilatedMat: Mat = Mat()
        Imgproc.dilate(morphedMat, dilatedMat, core)
        return dilatedMat
    }

    /**
     * The analyzeFrame function searches the biggest square (sudoku)
     * within a frame and is used within the "onCameraFrame()" Function.
     *
     * Input: frame as object of CvCameraViewFrame
     * Output: Mat which will be then displayed as frame
     *
     * */
    fun analyzeFrame( frame: CameraBridgeViewBase.CvCameraViewFrame ): Mat {
        Log.d("ComputerVision", "analyzeFrame()")

        var grayMat: Mat = frame.gray()
        var blurMat: Mat = Mat()
        var thresh: Mat = Mat()
        var contours = ArrayList<MatOfPoint>()
        var hierarchy = Mat() // Get Hierarchy Tree from OpenCV
        var biggest: MatOfPoint2f = MatOfPoint2f()
        var max_area = 0.0

        Imgproc.GaussianBlur( grayMat, blurMat, Size(5.0, 5.0), 0.0 )
        Imgproc.adaptiveThreshold( blurMat, thresh,255.0, 1,1, 11, 2.0 )
        Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)
        hierarchy.release()

        // For each contour approximate with polygon and find biggest area
        for (contour in contours) {
            var area = Imgproc.contourArea(contour) //
            if (area > 100) {
                var m: MatOfPoint2f = MatOfPoint2f() // TODO: DIES NOCHMAL CHECKEN
                m.fromList(contour.toList())

                var peri = Imgproc.arcLength(m, true)
                var approx: MatOfPoint2f = MatOfPoint2f()
                Imgproc.approxPolyDP(m, approx, 0.02 * peri, true)

                if (area > max_area && approx.total() == 4L) {
                    biggest = approx
                    max_area = area
                }
            }
        }

        // Finding outer box
        var displayMat: Mat = frame.rgba()
        var points: Array<Point> = biggest.toArray()
        var cropped = Mat()
        var t = 3

        if (points.size == 4) { // TODO: ELSE ? ...

            // TODO: WE HAVE TO DETERMINE WHICH COORDINATE IS "TOP LEFT", "TOP RIGHT", ...

            Log.d("MainActivity", "Point: x " + points[0].x + " y: " + points[0].y)
            Log.d("MainActivity", "Point: x " + points[1].x + " y: " + points[1].y)
            Log.d("MainActivity", "Point: x " + points[2].x + " y: " + points[2].y)
            Log.d("MainActivity", "Point: x " + points[3].x + " y: " + points[3].y)
            // Draw surrounding box
            var xDiff = abs(points[2].x - points[1].x ) /9
            var yDiff = abs(points[2].y - points[3].y ) /9

            Imgproc.line(displayMat, Point(points[0].x, points[0].y), Point(points[1].x, points[1].y), Scalar(255.0,0.0,0.0), 3 ) // oben rechts -> unten rechts
            Imgproc.line(displayMat, Point(points[1].x, points[1].y), Point(points[2].x, points[2].y), Scalar(255.0,0.0,0.0), 3 ) // oben links -> oben rechts
            Imgproc.line(displayMat, Point(points[2].x, points[2].y), Point(points[3].x, points[3].y), Scalar(255.0,0.0,0.0), 3 ) // oben links -> unten links
            Imgproc.line(displayMat, Point(points[3].x, points[3].y), Point(points[0].x, points[0].y), Scalar(255.0,0.0,0.0), 3 ) // unten links -> unten rechts

            var R: Rect = Rect( Point(points[0].x - t, points[0].y - t), Point(points[2].x + t, points[2].y + t) )
            if (displayMat.width() > 1 && displayMat.height() > 1) {
                cropped = Mat(displayMat, R)
                // TODO: CONVERT Mat TO Bitmap AND USE IT FOR DigitClassifier
            }
        }


        //
        //
        // TODO: use cropped to get the Sudoku as Bitmap!!!
        //
        //

        return displayMat
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
     * This function is used in analyzeFrame()
     *
     * Input: Array<Points>
     * Output: Array<Points> // sorted
     *
     * */
    private fun sortPointsArray(coordArray: Array<Point>): Array<Point> {
        return coordArray
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