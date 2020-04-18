package sse.goethe.arsudoku.ui.login

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import sse.goethe.arsudoku.MainActivity
import sse.goethe.arsudoku.R
import sse.goethe.arsudoku.User
/**
 * Implements a fragment that enables the user to register and login using the firebase DB.
 * After inputting his credentials, the firebase checks whether the data is valid. On success
 * the user is set and can proceed to receive his data (friends and games scanned in the past)
 * in the respective views.
 *
 * @author Nils Gormsen
 */
class LoginFragment : Fragment() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var auth: FirebaseAuth



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activity = activity as MainActivity?
        if (activity != null) {
            activity.stopCamera()
        }
        loginViewModel =
            ViewModelProviders.of(this).get(LoginViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_login, container, false)
        val registerButton: Button = root.findViewById(R.id.registerButton)
        val loginButton: Button = root.findViewById(R.id.loginButton)
        val emailTextView: TextView = root.findViewById(R.id.loginEmail)
        val passwordTextView: TextView = root.findViewById(R.id.loginPassword)
        val nameTextView: TextView = root.findViewById(R.id.loginName)
        val db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Set on click action for register button.
        registerButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                try {
                    auth.createUserWithEmailAndPassword(emailTextView.text.toString(), passwordTextView.text.toString())
                        .addOnCompleteListener(MainActivity()) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("success", "createUserWithEmail:success")
                                val user = auth.currentUser
                                // Create map of user data for firebase
                                val userData = hashMapOf(
                                    "email" to emailTextView.text.toString(),
                                    "name" to nameTextView.text.toString()
                                )
                                // Save data to firebase
                                db.collection("users").document(emailTextView.text.toString())
                                    .set(userData)
                                    .addOnSuccessListener {
                                        Log.d("success", "DocumentSnapshot successfully written!")
                                        activity!!.setGlobalUser(User(nameTextView.text.toString(), emailTextView.text.toString()))
                                        showRegisterSuccessDialog(root.context)
                                        activity.navigateHome()
                                    }
                                    .addOnFailureListener { e -> Log.w("error", "Error writing document", e) }
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                                showRegisterErrorDialog(root.context)
                            }

                        }
                }
                catch (e: IllegalArgumentException){
                    showRegisterErrorDialog(root.context)

                }

            }
        })

        // Set on click action for login functionality
        loginButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                try {
                    auth.signInWithEmailAndPassword(emailTextView.text.toString(), passwordTextView.text.toString())
                        .addOnCompleteListener(MainActivity()) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(ContentValues.TAG, "signInWithEmail:success")
                                val user = auth.currentUser
                                loginViewModel.setUser(User(nameTextView.text.toString(), emailTextView.text.toString()))
                                System.out.println(loginViewModel.user.value?.getEmail())
                                activity!!.setGlobalUser(User(nameTextView.text.toString(), emailTextView.text.toString()))
                                showLoginSuccessDialog(root.context)
                                activity.navigateHome()

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("success", "signInWithEmail:failure", task.exception)
                                showLoginErrorDialog(root.context)


                            }

                            // ...
                        }

                }
                catch (e: IllegalArgumentException){
                    Log.d("error", "empty string")
                    showLoginErrorDialog(root.context)

                }
                System.out.println("User data:"+ activity!!.getGlobalUser().getName())

            }

        })

        return root
    }

    /**
     * Shows a register success message to the user.
     */
    fun showRegisterSuccessDialog(context: Context){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Success")
            .setMessage("Register Successful!")
            .setCancelable(true)
            .setPositiveButton("Continue",
                DialogInterface.OnClickListener { dialog, which ->
                    dialog.cancel()
                })

        //Creating dialog box
        val dialog = builder.create()
        dialog.show()
    }

    /**
     * Shows a register failure message to the user.
     */
    fun showRegisterErrorDialog(context: Context){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Error")
            .setMessage("Register not successful! Please input valid data.")
            .setCancelable(true)
            .setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, which ->
                    dialog.cancel()
                })

        val dialog = builder.create()
        dialog.show()
    }

    /**
     * Shows a success message to the user.
     */
    fun showLoginSuccessDialog(context: Context){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Success")
            .setMessage("Login Successful!")
            .setCancelable(true)
            .setPositiveButton("Continue",
                DialogInterface.OnClickListener { dialog, which ->
                    dialog.cancel()
                })
        //Creating dialog box
        val dialog = builder.create()
        dialog.show()
    }

    /**
     * Shows a failure message to the user.
     */
    fun showLoginErrorDialog(context: Context){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Error")
            .setMessage("Login not successful! Please input valid data.")
            .setCancelable(true)
            .setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, which ->
                    dialog.cancel()

                    //                    Toast.makeText(
                })
        //Creating dialog box
        val dialog = builder.create()
        dialog.show()
    }

}


