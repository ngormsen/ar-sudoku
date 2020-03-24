package sse.goethe.arsudoku
import android.content.ContentValues
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
import android.os.Build
import java.io.IOException
import java.io.InputStream
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var globalUser: User = User("Hello", "world@gmail.com")
    private lateinit var navView: NavigationView
    private lateinit var game:Game
    val db = FirebaseFirestore.getInstance()

    val TAG = MainActivity::class.java.simpleName
    var  mOpenCvCameraView : CameraBridgeViewBase? = null

    /* Instance of Recognition Class */
    var recognition = Recognition(this)

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertGamestateToFirebase(): ArrayList<Int>{
        var gamestateForFirebase = ArrayList<Int>()
        for(row in game.getGamestate().getCurrentState()){
            for (column in row){
                gamestateForFirebase.add(column)
            }
        }
        return gamestateForFirebase
    }

    fun convertFirebaseToGamestate(firebaseGamestate: ArrayList<Int>): Array<IntArray>{
        var newState = arrayOf(
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0)
        )
        var listIdx = 0
        for (row in 0..8) {
            for (column in 0..8) {
                newState[row][column] = firebaseGamestate[listIdx]
                listIdx += 1
            }
        }
        return newState
    }


    fun printState(state: Array<IntArray>){
        var n = 9
        for (i in 0 until n) {
            for (j in 0 until n) {
                print(state[i][j].toString())
                if (Math.floorMod(j, 3) == 2 && j < n - 1)
                    print(" ")
            }
            println()
            if (Math.floorMod(i, 3) == 2 && i < n - 1) println()
        }
    }

    fun compareGamestates(state1: Array<IntArray>, state2: Array<IntArray>): Boolean{
        /**
         * Returns true if the gamestates are not the same
         */
        for (row in 0..8){
            for (column in 0..8) {
                if (state1[row][column] != state2[row][column]) {
                    return true // If one number is different we return true
                }
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkIfGameExistsInDatabase(): Boolean{
        /**
         * Returns true if the last entry in the firebase is not the same game
         */
        var exists = false
        db.collection("users").document(getGlobalUser().getEmail()).collection("games")
            .orderBy("date")
            .limitToLast(1)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    var firebaseState = convertFirebaseToGamestate(document.data?.get("gamestate") as ArrayList<Int>)
                    exists = compareGamestates(firebaseState, game.getGamestate().getCurrentState()) // if one number is different we get true
                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
        return exists
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveGameToDatabase(){
        val gameMap = hashMapOf(
            "date" to game.getDate(),
            "user" to game.getEmail(),
            "gamestate" to convertGamestateToFirebase()
        )

        db.collection("users").document(getGlobalUser().getEmail()).collection("games").document(game.getDate())
            .set(gameMap)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setGame(sudoku: Sudoku){
        game = Game(getGlobalUser().getEmail(), sudoku)

    }

    fun getGame(): Game{
        return game
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val solver = Sudoku( arrayOf( //
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

//        mOpenCvCameraView = fragment_CameraView as CameraBridgeViewBase //TODO uncomment
//        mOpenCvCameraView?.apply {
//            visibility = SurfaceView.VISIBLE
//            setCvCameraViewListener(this@MainActivity)
//        }

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
        setGlobalUser(User("Nils", "nils.gormsen@googlemail.com"))

        // Create game
        setGame(solver)
        println("document in database:")

        if(checkIfGameExistsInDatabase() == true){
            saveGameToDatabase() // TODO: fix logic error and handle empty list case
        }


    }

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
        if(inputFrame == null){
            Log.e(TAG, "Input frame is null!!")
        }
        return inputFrame!!.rgba()
    }
}



