package sse.goethe.arsudoku.ui.history

import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.baoyz.swipemenulistview.SwipeMenu
import com.baoyz.swipemenulistview.SwipeMenuCreator
import com.baoyz.swipemenulistview.SwipeMenuItem
import com.baoyz.swipemenulistview.SwipeMenuListView
import com.google.firebase.firestore.FirebaseFirestore
import sse.goethe.arsudoku.MainActivity
import sse.goethe.arsudoku.R
import sse.goethe.arsudoku.Sudoku
import sse.goethe.arsudoku.ui.friends.FriendsViewModel
import java.util.*
import kotlin.collections.ArrayList
/**
 * Implements a fragment that shows all the games scanned by the current user.
 * The user can delete and play individual game scanned in the past or sent to him by friends.
 *
 * @author Nils Gormsen
 */
class HistoryFragment : Fragment() {

    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var friendsViewModel: FriendsViewModel
    private val history = ArrayList<String>()
    private val users = ArrayList<String>()  // Transfer to viewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activity = activity as MainActivity?
        if (activity != null) {
            activity.stopCamera()
        }

        historyViewModel =
            ViewModelProviders.of(this).get(HistoryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_history, container, false)

        val textView: TextView = root.findViewById(R.id.text_history)

        historyViewModel.text.observe(this, Observer {
            textView.text = it
            System.out.println(it)
        })

        val listViewHistory : SwipeMenuListView = root.findViewById(R.id.swipeMenuHistory)

        // Get firebase instance
        val db = FirebaseFirestore.getInstance()

        // Create list adapter for SwipeMenu
        val adapter = ArrayAdapter(root.context, android.R.layout.simple_list_item_1, history)
        // Create SwipeMenu
        val creator = SwipeMenuCreator { menu ->
            // create "Delete" item
            val deleteItem = SwipeMenuItem(
                root.context
            )
            // set item width
            deleteItem.width = 170
            // set item title
            deleteItem.title = "Delete"
//            // set a icon
            deleteItem.setIcon(R.drawable.cancel)

            // set item title fontsize
            deleteItem.titleSize = 15
            // set item title font color
            deleteItem.titleColor = Color.BLACK
            // add to menu
            menu.addMenuItem(deleteItem)

            // create "Play" item
            val sendItem = SwipeMenuItem(
                root.context
            )
            // set item width
            sendItem.title = "Play"
            sendItem.titleColor = Color.BLACK
            sendItem.titleSize = 15


            sendItem.width = 170
            // set a icon
            sendItem.setIcon(R.drawable.play_circle)
            // add to menu
            menu.addMenuItem(sendItem)
        }
        listViewHistory.setMenuCreator(creator)
        listViewHistory.setAdapter(adapter)


        // Set database listener for history data
        db.collection("users").document(activity!!.getGlobalUser().getEmail()).collection("games")
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
            @RequiresApi(Build.VERSION_CODES.O)
            override
            fun onMenuItemClick(position: Int, menu: SwipeMenu, index: Int): Boolean {
                val activity = activity as MainActivity?
                when (index) {
                    0 -> {
                        val activity = activity as MainActivity?
                        Log.d("succes", "onMenuItemClick: clicked item " + index)
                        db.collection("users").document(activity!!.getGlobalUser().getEmail())
                            .collection("games").document(history[position])
                            .delete()

                            .addOnSuccessListener {
                                adapter.notifyDataSetChanged()
                            }
                            .addOnFailureListener { e -> Log.w("error", "Error deleting document", e) }
                    }
                    1 -> {
                        Log.d("succes", "onMenuItemClick: clicked item " + index)
                        Log.d("which item", history[position])
                        db.collection("users").document(activity!!.getGlobalUser().getEmail())
                            .collection("games").document(history[position])
                            .get()
                            .addOnSuccessListener {document ->
                                activity.printState( activity.convertFirebaseToGamestate(document.data?.get("gamestate") as ArrayList<Int>))
                                activity.setGame(Sudoku(activity.convertFirebaseToGamestate(document.data?.get("gamestate") as ArrayList<Int>)))
                                activity.navigateToPlay()
                            }
                            .addOnFailureListener { e -> Log.w("error", "Error deleting document", e) }

                    }
                }// open
                // delete
                // false : close the menu; true : not close the menu
                return false
            }
        })


        return root
    }


    fun showFriendList(context: Context, item: String){
        val activity = activity as MainActivity?
        val builder = AlertDialog.Builder(context)
        val db = FirebaseFirestore.getInstance()
        val selectedItems = ArrayList<Int>() // Where we track the selected items

        db.collection("users").document(activity!!.getGlobalUser().getEmail()).collection("friends")
            .get()
            .addOnSuccessListener { documents ->
                for ((idx, document) in documents.withIndex()) {
                    Log.d("Data", "${document.id} => ${document.data}")
                    users.add(idx, document.data.get("email" ).toString())
                }


            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

        val array = Array(users.size) {
            users[it]
        }

        builder.setTitle("Send to a friend:")
//            .setMessage("Login not successful! Please input valid data.")

            .setItems(array,
                DialogInterface.OnClickListener { dialog, which ->
                    System.out.println("Send " + item + " to friend: " + users.get(which))
                    val gameData = hashMapOf(
                        "date" to item
                    )
                    println(gameData["date"])
                })


        val dialog = builder.create()
        dialog.show()
    }

}
