package sse.goethe.arsudoku.ui.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.firestore.FirebaseFirestore
import sse.goethe.arsudoku.R

class HistoryFragment : Fragment() {

    private lateinit var historyViewModel: HistoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        historyViewModel =
            ViewModelProviders.of(this).get(HistoryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_history, container, false)

        val db = FirebaseFirestore.getInstance()


        val user = hashMapOf(
            "first" to "Ada",
            "last" to "Lovelace",
            "born" to 1815
        )

        db.collection("users").document("lovelace")
            .set(user)
            .addOnSuccessListener { documentReference ->
                Log.d("success", "DocumentSnapshot added with ID: ${documentReference}")
            }
            .addOnFailureListener { e ->
                Log.w("nosuccess", "Error adding document", e)
            }


        val textView: TextView = root.findViewById(R.id.text_history)

        historyViewModel.text.observe(this, Observer {
            textView.text = it
        })

        return root
    }
}
