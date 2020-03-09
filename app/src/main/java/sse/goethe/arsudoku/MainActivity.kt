package sse.goethe.arsudoku
import android.content.Context
import sse.goethe.arsudoku.ml.Recognition
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.content.res.AssetManager
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Camera
import java.io.IOException
import java.io.InputStream
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame // test function onCameraFrame

import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
//import org.opencv.imgproc.Imgproc.findContours
import org.opencv.imgproc.Imgproc
import kotlin.math.abs


class MainActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var globalUser: User = User("Hello", "world@gmail.com")
    private lateinit var navView: NavigationView

    val TAG = MainActivity::class.java.simpleName
    var  mOpenCvCameraView : CameraBridgeViewBase? = null

    /* Instance of Recognition Class */
    var recognition = Recognition(this)

    var frameCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val solver = Sudoku( arrayOf(
            intArrayOf(0, 0, 1, 5, 0, 0, 0, 0, 6),
            intArrayOf(0, 6, 0, 2, 1, 0, 0, 0, 4),
            intArrayOf(9, 0, 2, 0, 0, 6, 0, 1, 3),
            intArrayOf(0, 0, 0, 4, 0, 0, 1, 8, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 5, 7, 0, 0, 2, 0, 0, 0),
            intArrayOf(7, 3, 0, 6, 0, 0, 2, 0, 1),
            intArrayOf(5, 0, 0, 0, 3, 1, 0, 6, 0),
            intArrayOf(6, 0, 0, 0, 0, 5, 9, 0, 0)
        ))

        println(solver.solve())

        mOpenCvCameraView = fragment_CameraView as CameraBridgeViewBase
        mOpenCvCameraView?.apply {
            visibility = SurfaceView.VISIBLE
            setCvCameraViewListener(this@MainActivity)
        }

        /* ############################################
        *  RECOGNIZE SUDOKU TEST
        *  ############################################ */



        // #########################################################################################
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        navView= findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_history, R.id.nav_friends,
                R.id.nav_tools, R.id.nav_login, R.id.nav_logout
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)



        Log.d(TAG, "SUDOKU-DIGITS: " + recognition.sudokuPredictedDigits[0][0])
        setGlobalUser(User("name", "email"))


    }

    // THIS IS A FUNCTION TO TEST OPENCV AND BELONGS TO THE OPENVISION PART
    // IF IT IS WORKING

    // implementation of interface CvCameraViewFrame !

    /*
    interface CvCameraViewFrame {
        fun rgba(): Mat
        fun gray(): Mat
    }

    fun onRunningFrame( frame: CvCameraViewFrame ): Mat {
        // greyscale the frame
        var inpFrame = frame.gray()
        // apply adaptive threshold
        Imgproc.adaptiveThreshold(inpFrame, inpFrame, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 10.0)

        var contours = ArrayList<MatOfPoint>() // destination for findContours()
        var hierarchy = Mat() //

        Imgproc.findContours(inpFrame, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

        hierarchy.release() // for deallocation

        var biggest = MatOfPoint2f()
        var max_area = 0.0
        var approxContour = Mat() // holds the approximated contour

        // approx. contours by polygons
        for (contour in contours) {
            var area = Imgproc.contourArea(contour)
            if (area > 100) {
                var tmp: MatOfPoint2f = MatOfPoint2f(contour)
                var peri = Imgproc.arcLength(tmp, true)
                var approx: MatOfPoint2f = MatOfPoint2f()
                Imgproc.approxPolyDP(tmp, approx, 0.02 * peri, true)
                if ( (area > max_area) && (approx.total() == 4L) ) {
                    biggest = approx
                    max_area = area
                }
            }
        }

        // find surrounding box now
        var displayMat: Mat = frame.rgba()
        var points: Array<Point> = biggest.toArray()
        var cropped: Mat = Mat()
        var t: Int = 3

        if (points.size >= 4) {
            // draw surrounding box
            Imgproc.line(displayMat, Point(points[0].x, points[0].y), Point(points[0].x, points[0].y), Scalar(255.0,0.0,0.0), 2 )
            Imgproc.line(displayMat, Point(points[1].x, points[1].y), Point(points[2].x, points[2].y), Scalar(255.0,0.0,0.0), 2 )
            Imgproc.line(displayMat, Point(points[2].x, points[2].y), Point(points[3].x, points[3].y), Scalar(255.0,0.0,0.0), 2 )
            Imgproc.line(displayMat, Point(points[3].x, points[3].y), Point(points[0].x, points[0].y), Scalar(255.0,0.0,0.0), 2 )

            // crop the image
            var r: Rect = Rect( Point(points[0].x - t, points[0].y - t), Point(points[2].x + t, points[2].y + t) )
            if (displayMat.width() > 1 && displayMat.height() > 1) {
                cropped = Mat(displayMat, r)
            }
        }
        return displayMat
    }
    */


    // +++++++++++++++++++++++++++++++++++++

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.action_settings -> {
                println("Hello logout")
                setGlobalUser(User("name", "email"))
//                firebase.auth().signOut().then(function() {
//                    // Sign-out successful.
//                }).catch(function(error) {
//                    // An error happened.
//                });

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    /*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    * functions for using the DigitClassifier from RecognitonClass
    * author: David Machajewski
    *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

//    override fun onDestroy() {
//        recognition.close()
//        super.onDestroy()
//    }


    companion object {
        // just to use it for Log's
        private const val TAG = "MainActivity"}
    fun setHeaderCredentials(user:User){
        // Get header access
        val headerView : View = navView.getHeaderView(0)
        val navUserName : TextView = headerView.findViewById(R.id.headerName)
        val navUserEmail : TextView = headerView.findViewById(R.id.headerEmail)

        navUserName.text = user.getName()
        navUserEmail.text = user.getEmail()

    }

    fun stopCamera(){
        mOpenCvCameraView?.disableView()
        println("Camera stopped")
    }


    fun navigateHome() {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigate(R.id.nav_home)
    }

    fun setGlobalUser(user: User){
        globalUser = user
        setHeaderCredentials(user)
    }

    fun getGlobalUser(): User{
        return globalUser
    }



    private val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    mOpenCvCameraView?.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        if(!OpenCVLoader.initDebug()){
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback)
        }else{
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }

    }

    override fun onPause() {
        super.onPause()
        mOpenCvCameraView?.disableView()

    }

    override fun onDestroy() {
        recognition.close()
        super.onDestroy()
        mOpenCvCameraView?.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {

    }

    override fun onCameraViewStopped() {

    }


    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        this.frameCounter += 1
        // This method is invoked when delivery of the frame needs to be done.
        // The returned values - is a modified frame which needs to be displayed on the screen
        if(inputFrame == null){
            Log.e(TAG, "Input frame is null!!")
        }

        // init matrices and apply adaptiveThreshold
        // create List for contours and use findContours function
        // for each contour use contourArea()
            // approximate with polygons
            // determine max area

        // find the outer box
        // draw lines with Core.line(...)
        // crop img

        // return displayMat

        Log.d("FRAME:", "onCameraFrame() Method")


        var outputFrame: Mat
        if (inputFrame != null) {
            //Imgproc.adaptiveThreshold(inputFrame.gray(), outputFrame, 255.0,1,1,11,2.0)

            // TODO: Call the function each X-th frame where X > k? frame
            // X < 10 will cause threadng problems
            if (this.frameCounter > 0 ) { // k = 25 for now
                outputFrame = analyzeFrame(inputFrame)
                this.frameCounter = 0
                return outputFrame
            } else { return inputFrame!!.rgba()}

        } else {
            return inputFrame!!.rgba()
        }
        //return outputFrame!! // comment the other return then

        //return inputFrame!!.rgba()
        //return inputFrame!!.gray()
    }

    /**
     * Analyses each k-th frame for the biggest square
     * */
    fun analyzeFrame(frame: CvCameraViewFrame ): Mat {

        // OUTER SQUARE NO MATTER IF ITS A SUDOKU OR OTHER TYPE OF SQUARE
        // CAN BE RECOGNIZED NOW.
        // TODO: IMPLEMENT A HEURISTIC TO DETERMINE IF IT IS A SUDOKU OR NOT
        // WHAT IS HAPPENING IF THERE IS NOT A SUDOKU WITHIN SCREEN


        var grayMat: Mat = frame.gray()
        var blurMat: Mat = Mat()
        Imgproc.GaussianBlur(grayMat, blurMat, Size(5.0,5.0), 0.0)
        var thresh: Mat = Mat()
        Imgproc.adaptiveThreshold(blurMat, thresh, 255.0,1,1,11,2.0)

        var contours = ArrayList<MatOfPoint>() // destination for findContours()
        var hierarchy = Mat() //

        // TODO: WHAT IF IT IS NOT POSSIBLE TO FIND A CONTOUR? -> Use try, catch
        Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)
        hierarchy.release() // for deallocation

        var biggest: MatOfPoint2f = MatOfPoint2f()
        var max_area = 0.0

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

        // Find the outer box
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
            var xDiff = abs(points[2].x - points[1].x )/9
            var yDiff = abs(points[2].y - points[3].y )/9

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
        return displayMat
    }




}

















