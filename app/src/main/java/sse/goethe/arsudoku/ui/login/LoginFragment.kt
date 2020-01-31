package sse.goethe.arsudoku.ui.login

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import sse.goethe.arsudoku.MainActivity
import sse.goethe.arsudoku.R

class LoginFragment : Fragment() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        val activity: MainActivity = MainActivity()
//        val currentUser = auth.currentUser
//        updateUI(currentUser)



//        val name: TextView = root.findViewById(R.id.loginName)
//        loginViewModel.userEmail.observe(this, Observer {
//            name.text = it
//        })


        registerButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {

                auth.createUserWithEmailAndPassword(emailTextView.text.toString(), passwordTextView.text.toString())
                    .addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("success", "createUserWithEmail:success")
                            Toast.makeText(root.context, "Register successful.",
                                Toast.LENGTH_LONG).show()
                            val user = auth.currentUser
                            val userData = hashMapOf(
                                "email" to emailTextView.text.toString(),
                                "name" to nameTextView.text.toString()
                            )
                            db.collection("users").document(emailTextView.text.toString())
                                .set(userData)
                                .addOnSuccessListener {
                                    Log.d("success", "DocumentSnapshot successfully written!")
//                                    Snackbar.make(root.rootView, "added", Snackbar.LENGTH_LONG)
//                                        .setAction("Action", null).show()

                                }
                                .addOnFailureListener { e -> Log.w("error", "Error writing document", e) }

//                    updateUI(user)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
//                            Toast.makeText(root.context, "Authentication failed.",
//                                Toast.LENGTH_SHORT).show()
//                    updateUI(null)
                        }

                        // ...
                    }
            }
        })

        loginButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                auth.signInWithEmailAndPassword(emailTextView.text.toString(), passwordTextView.text.toString())
                    .addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(ContentValues.TAG, "signInWithEmail:success")
                            val user = auth.currentUser
                            loginViewModel.setUserEmail(emailTextView.text.toString())
                            loginViewModel.setUserName(nameTextView.text.toString())
//                            updateUI(user)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("success", "signInWithEmail:failure", task.exception)
//                            Toast.makeText(root.context, "Authentication failed.",
//                                Toast.LENGTH_SHORT).show()
//                            updateUI(null)
                        }

                        // ...
                    }
            }

        })

        return root
    }
}