package sse.goethe.arsudoku.ui.history

import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
import sse.goethe.arsudoku.ui.friends.FriendsViewModel
import java.util.*
import kotlin.collections.ArrayList

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

        historyViewModel =
            ViewModelProviders.of(this).get(HistoryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_history, container, false)
        val activity = activity as MainActivity?

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
            // set item background
            deleteItem.background = ColorDrawable(
                Color.rgb(
                    0xF9, 0x3F,
                    0x25
                )
            )
            // set item width
            deleteItem.width = 170
            // set item title
            deleteItem.title = "Delete"
//            // set a icon
//            deleteItem.setIcon(R.drawable.ic_menu_send)

            // set item title fontsize
            deleteItem.titleSize = 18
            // set item title font color
            deleteItem.titleColor = Color.BLACK
            // add to menu
            menu.addMenuItem(deleteItem)

            // create "Send" item
            val sendItem = SwipeMenuItem(
//                root.getApplicationContext<Context>()
                root.context
            )
            // set item background
            sendItem.background = ColorDrawable(
                Color.rgb(
                    75,
                    219, 87
                )
            )
            // set item width
            sendItem.width = 170
            // set a icon
            sendItem.setIcon(R.drawable.ic_menu_send)
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
            override
            fun onMenuItemClick(position: Int, menu: SwipeMenu, index: Int): Boolean {
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
                        showFriendList(root.context, history[position])
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
//        var users = ArrayList<String>()
//        var users = arrayOf("1@gmail.com", "", "", "","", "", "", "","", "", "", "")
//        var users = arrayOfNulls<String>(10)

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
//                    db.collection("users").document(users.get(which)).collection("games").document(item)
//                        .set(gameData)
//                        .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
//                        .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

                    // The 'which' argument contains the index position
                    // of the selected item
                })


        val dialog = builder.create()
        dialog.show()
    }

}


//        val userArray = arra
//        val userArray = arrayOfNulls<String>(users.size)
//        for ((index, value) in someList.withIndex()){
//            println("$index: $value")
//            users.add(index, value)
//        }

//        val array = arrayOfNulls<String>(users.size)
//        users.toArray(array)
//        var userArray = arrayOf(Arrays.toString(array))

//        var someList = arrayListOf<String>("Hello", "Again")
//        var userArray = Array<String>(users.size, )
//        val array = arrayOfNulls<String>(users.size)
////        println(users.size)
////        println(array.size)
//        println(array.toString())
//
//        for ((index, value) in users.withIndex()){
//            println("$index: $value")
//            array.set(index, value.toString())
//        }
//        println(array.toString())
//        var userArray = users.toTypedArray()
//        println(array.toCollection())





//            .setCancelable(false)
//            .setPositiveButton("OK",
//                DialogInterface.OnClickListener { dialog, which ->
//
//                    //                    Toast.makeText(
////                        context,
////                        "Selected Option: Continue",
////                        Toast.LENGTH_SHORT
////                    ).show()
//                })
////                                    .setNegativeButton("No",
////                                        DialogInterface.OnClickListener { dialog, which ->
////                                            Toast.makeText(
////                                                root.context,
////                                                "Selected Option: No",
////                                                Toast.LENGTH_SHORT
////                                            ).show()
////                                        })
//        //Creating dialog box