package sse.goethe.arsudoku.ui.history

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.baoyz.swipemenulistview.SwipeMenu
import com.baoyz.swipemenulistview.SwipeMenuCreator
import com.baoyz.swipemenulistview.SwipeMenuItem
import com.baoyz.swipemenulistview.SwipeMenuListView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.nav_header_main.*
import sse.goethe.arsudoku.R

class HistoryFragment : Fragment() {

    private lateinit var historyViewModel: HistoryViewModel
    private val history = ArrayList<String>()
    private lateinit var userEmail: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        historyViewModel =
            ViewModelProviders.of(this).get(HistoryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_history, container, false)
        val listViewHistory : SwipeMenuListView = root.findViewById(R.id.swipeMenuHistory)

        // Get firebase instance
        val db = FirebaseFirestore.getInstance()

        // Create list adapter for SwipeMenu
        val adapter = ArrayAdapter(root.context, android.R.layout.simple_list_item_1, history)
        // Create SwipeMenu
        val creator = SwipeMenuCreator { menu ->
            // create "open" item
            val openItem = SwipeMenuItem(
                root.context
            )
            // set item background
            openItem.background = ColorDrawable(
                Color.rgb(
                    0xC9, 0xC9,
                    0xCE
                )
            )
            // set item width
            openItem.width = 170
            // set item title
            openItem.title = "Open"
            // set item title fontsize
            openItem.titleSize = 18
            // set item title font color
            openItem.titleColor = Color.WHITE
            // add to menu
            menu.addMenuItem(openItem)

            // create "delete" item
            val deleteItem = SwipeMenuItem(
//                root.getApplicationContext<Context>()
                root.context
            )
            // set item background
            deleteItem.background = ColorDrawable(
                Color.rgb(
                    0xF9,
                    0x3F, 0x25
                )
            )
            // set item width
            deleteItem.width = 170
            // set a icon
            deleteItem.setIcon(R.drawable.ic_menu_send)
            // add to menu
            menu.addMenuItem(deleteItem)
        }
        listViewHistory.setMenuCreator(creator)
        listViewHistory.setAdapter(adapter)


        // Set database listener for history data
        db.collection("users").document("nils.gormsen@googlemail.com").collection("games")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w("fail", "Listen failed.", e)
                    return@addSnapshotListener
                }
                history.clear()

                for (doc in value!!) {
                    doc.getString("date")?.let {
                        history.add(it)
                        adapter.notifyDataSetChanged()
                    }
                }
                Log.d("success", "Current games in history: $history")
            }


        // Set action for menu swipe buttons
        listViewHistory.setOnMenuItemClickListener(object : SwipeMenuListView.OnMenuItemClickListener {
            override
            fun onMenuItemClick(position: Int, menu: SwipeMenu, index: Int): Boolean {
                when (index) {
                    0 -> {
                        Log.d("succes", "onMenuItemClick: clicked item " + index)
                        db.collection("cities").document("DC")
                            .collection("games").document(history.get(index))
                            .delete()
                            .addOnSuccessListener { Log.d("success", history.get(index)) }
                            .addOnFailureListener { e -> Log.w("error", "Error deleting document", e) }

                        adapter.notifyDataSetChanged()


                    }
                    1 -> {
                        Log.d("succes", "onMenuItemClick: clicked item " + index)

                    }
                }// open
                // delete
                // false : close the menu; true : not close the menu
                return false
            }
        })


        return root
    }
}

//
//val textView: TextView = root.findViewById(R.id.text_history)
//
//historyViewModel.text.observe(this, Observer {
//    textView.text = it
//})
