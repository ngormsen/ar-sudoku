package sse.goethe.arsudoku

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat

/**
 *      https://developer.android.com/training/permissions/requesting
 */
class RequestPermission(context : Context, activity : Activity) {

    val context = context
    val activity = activity

    val MY_PERMISSIONS_REQUEST_CAMERA : Int = 0

    fun check() : Boolean {

        // Check if the camera permission is already available
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Camera permission is already available
            return true
        }
        else {
            /*  Camera permission has not been granted

                Provide an additional rationale to the user if the permission was not granted
                and the user would benefit from additional context for the use of the permission.
             */
            /*
            if (shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
                Toast.makeText(context, "Camera permission is needed to show the camera preview.", Toast.LENGTH_SHORT).show()
            }
             */
            // Request camera permission
            requestPermissions(activity, arrayOf(Manifest.permission.CAMERA),MY_PERMISSIONS_REQUEST_CAMERA)
            return false
        }
    }
}