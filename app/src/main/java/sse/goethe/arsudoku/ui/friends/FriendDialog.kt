package sse.goethe.arsudoku.ui.friends

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import sse.goethe.arsudoku.MainActivity
import sse.goethe.arsudoku.R

//https://blog.mindorks.com/implementing-dialog-fragment-in-android

class FriendDialog : DialogFragment() {

    val db = FirebaseFirestore.getInstance()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (arguments != null)
        {
            if (arguments?.getBoolean("notAlertDialog")!!)
            {
                return super.onCreateDialog(savedInstanceState)
            }
        }
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Hello")
        builder.setMessage("Hello! I am Alert Dialog")
        builder.setPositiveButton("Cool", object: DialogInterface.OnClickListener {
            override fun onClick(dialog:DialogInterface, which:Int) {
                dismiss()
            }
        })
        builder.setNegativeButton("Cancel", object: DialogInterface.OnClickListener {
            override fun onClick(dialog:DialogInterface, which:Int) {
                dismiss()
            }
        })
        return builder.create()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_friends_dialog, container, false)
    }
    override fun onViewCreated(view:View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val editText = view.findViewById<EditText>(R.id.inFriend)
        if (arguments != null && !TextUtils.isEmpty(arguments?.getString("mobile")))
            editText.setText(arguments?.getString("mobile"))

        val btnDone = view.findViewById<Button>(R.id.btnDone)
        btnDone.setOnClickListener(object: View.OnClickListener {
            override fun onClick(view:View) {
//                val dialogListener = activity as DialogListener
//                dialogListener.onFinishEditDialog(editText.text.toString())
                System.out.println(editText.text.toString())
                val game = hashMapOf(
                    "email" to editText.text.toString()
                )
                val activity = activity as MainActivity?

                db.collection("users")
                    .whereEqualTo("email", editText.text.toString())
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.documents.isEmpty()){
                            println("not empty")
                            db.collection("users").document(activity!!.getGlobalUser().getEmail())
                                .collection("friends").document(editText.text.toString())
                                .set(game)
                                .addOnSuccessListener { Log.d("success", "DocumentSnapshot successfully written!") }
                                .addOnFailureListener { e -> Log.w("success", "Error writing document", e) }
                            dismiss()
                        }
                        for (document in documents) {
                            Log.d(TAG, "${document.id} => ${document.data}")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting documents: ", exception)
                    }

//                Snackbar.make(view.rootView, "added", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()



            }
        })
    }
    override fun onResume() {
        super.onResume()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Hey", "onCreate")
        var  setFullScreen = false
        if (arguments != null) {
            setFullScreen = requireNotNull(arguments?.getBoolean("fullScreen"))
        }
        if (setFullScreen)
            setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }
    override fun onDestroyView() {
        super.onDestroyView()
    }
    interface DialogListener {
        fun onFinishEditDialog(inputText:String)
    }
}