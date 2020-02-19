package sse.goethe.arsudoku
import android.content.Context
import sse.goethe.arsudoku.ml.DigitClassifier
import sse.goethe.arsudoku.ml.Result
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
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
import android.util.Log
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    //load image for test as Bitmap


    // variable for results

    /* for digit classifier class*/
    //private lateinit var photoImage: Bitmap
    //private lateinit var classifier: DigitClassifier
    private var digitClassifier = DigitClassifier(this)


    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        /* SOME TESTS FOR CLASSIFIER CLASS */
        //resul.click()
        //classifier = DigitClassifier(assets) //assets returns AssetManager object
        //println("SOME INTs: " + classifier.results)

        /*++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        * Set up the digit classifier
        * author: David Machajewski
        *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
        digitClassifier.initializeInterpreter()

        /* test with a bitmap */
        var testbitmap: Bitmap = digitClassifier.getBitmapFromAsset(this, "mnist_7.PNG")

        var predictedClass: String = digitClassifier.classify(testbitmap)
        Log.d(TAG, "The predicted class is: " + predictedClass)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }






    /*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    * functions for using the DigitClassifier
    * author: David Machajewski
    *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    override fun onDestroy() {
        digitClassifier.close()
        super.onDestroy()
    }


    companion object {
        // just to use it for Log's
        private const val TAG = "MainActivity"
    }
}
