package sse.goethe.arsudoku.ui.friends

import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.baoyz.swipemenulistview.SwipeMenu
import com.baoyz.swipemenulistview.SwipeMenuCreator
import com.baoyz.swipemenulistview.SwipeMenuItem
import com.baoyz.swipemenulistview.SwipeMenuListView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import sse.goethe.arsudoku.Gamestate
import sse.goethe.arsudoku.MainActivity
import sse.goethe.arsudoku.R
import java.io.Serializable
import java.lang.IllegalArgumentException

/**
 * Implements a fragment that shows all the friends of the current user.
 * Allows for adding, deleting friends and sending recently scanned games to them.
 *
 * @author Nils Gormsen
 */
class FriendsFragment : Fragment() {

    private lateinit var friendsViewModel: FriendsViewModel
    private val users = ArrayList<String>()  // Transfer to viewModel in production version
    var games = arrayOf("", "", "","", "", "","", "", "","", "", "","","", "",
        "","", "", "","", "", "","","", "", "","",
        "", "","", "", "","","", "", "","", "", "","", "", "","","",
        "", "","", "", "","", "", "","","", "", "","", "", "","", "", "","","",
        "", "","", "", "","", "", "","", "", "","","", "", "","", "", "","", "", "","","", "", "","",
        "", "","", "", "","","", "", "","", "", "","", "", "","","",
        "", "","", "", "","", "", "","","", "", "","", "", "","", "", "","","",
        "", "","", "", "","", "", "","", "", "","","", "", "","", "", "","", "", "","","", "", "","",
        "", "","", "", "","","", "", "","", "", "","", "", "","","",
        "", "","", "", "","", "", "","","", "", "","", "", "","", "", "","")  // TODO: fix array problem
    val db = FirebaseFirestore.getInstance()
//    lateinit var gamestate: ArrayList<Int>;

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Access the MainActivity
        val activity = activity as MainActivity?

        if (activity != null) {
            activity.stopCamera()
        }
        friendsViewModel =
            ViewModelProviders.of(this).get(FriendsViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_friends, container, false)

        // Implements button to add friends
        val fab: FloatingActionButton = root.findViewById(R.id.fab)
        fab.setOnClickListener { view ->

            val dialogFragment = FriendDialog()
            val bundle = Bundle()
            bundle.putBoolean("notAlertDialog", true)
            dialogFragment.arguments = bundle
            val ft = (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
            val prev = (activity as AppCompatActivity).supportFragmentManager.findFragmentByTag("dialog")
            if (prev != null)
            {
                ft.remove(prev)
            }
            ft.addToBackStack(null)
            dialogFragment.show(ft, "dialog")
        }

        // Creates the SwipeMenu
        val listView : SwipeMenuListView = root.findViewById(R.id.swipeMenu)
        // Get access to the database
        val db = FirebaseFirestore.getInstance()
        // The adapter provides the items for the SwipeMenu
        val adapter = ArrayAdapter(root.context, android.R.layout.simple_list_item_1, users)
        // Configures the SwipeMenu
        val creator = SwipeMenuCreator { menu ->
            // create "open" item
            val openItem = SwipeMenuItem(
                root.context
            )
            // set item width
            openItem.width = 170
            // set item title
            openItem.title = "Delete"
            // set item title fontsize
            openItem.titleSize = 15
            // set item title font color
            openItem.titleColor = Color.BLACK
            openItem.setIcon(R.drawable.cancel)


            // add to menu
            menu.addMenuItem(openItem)

            // create "delete" item
            val sendItem = SwipeMenuItem(
                root.context
            )
            sendItem.title = "Send"
            // set item title fontsize
            sendItem.titleSize = 15
            // set item title font color
            sendItem.titleColor = Color.BLACK

            // set item width
            sendItem.width = 170
            // set a icon
            sendItem.setIcon(R.drawable.ic_menu_send)
            // add to menu
            menu.addMenuItem(sendItem)
        }          // set creator

        // Attaches the Creator and Adapter to the Swipe Menu
        listView.setMenuCreator(creator)
        listView.setAdapter(adapter)
        populateGameArray()
        adapter.notifyDataSetChanged()


        // Set database listener for friend data
        db.collection("users").document(activity!!.getGlobalUser().getEmail())
            .collection("friends")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w("fail", "Listen failed.", e)
                    return@addSnapshotListener
                }
                users.clear()

                for (doc in value!!) {
                    doc.getString("email")?.let {
                        users.add(it)
                        adapter.notifyDataSetChanged()
                    }
                }
                Log.d("success", "Current friends in friends: $users")
            }

        // Set action for menu swipe buttons
        listView.setOnMenuItemClickListener(object : SwipeMenuListView.OnMenuItemClickListener {
            override
            fun onMenuItemClick(position: Int, menu: SwipeMenu, index: Int): Boolean {
                when (index) {
                    0 -> {
                        val activity = activity as MainActivity?

                        Log.d("succes", "onMenuItemClick: clicked item " + index)
                        db.collection("users").document(activity!!.getGlobalUser().getEmail())
                            .collection("friends").document(users[position])
                            .delete()
                            .addOnSuccessListener { Log.d("success", "success") }
                            .addOnFailureListener { e -> Log.w("error", "Error deleting document", e) }

                    }
                    1 -> {
                        Log.d("succes", "onMenuItemClick: clicked item " + index)
                        buildGameList(root.context, users[position])
                    }
                }// open
                // delete
                // false : close the menu; true : not close the menu
                return false
            }
        })
        return root
    }
    /**
     * Populates the game array with the data from the firebase.
     */
    fun populateGameArray(){
        val activity = activity as MainActivity?
        db.collection("users").document(activity!!.getGlobalUser().getEmail()).collection("games")
            .get()
            .addOnSuccessListener { documents ->
                for ((idx, document) in documents.withIndex()) {
                    Log.d("Data", "${document.id} => ${document.data}")
                    games.set(idx, document.data.get("date").toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    /**
     * Creates a dialog containing the games scanned by the user in the past.
     * Clicking on an item will send the game to the respective friend and update the
     * database.
     *
     * @param context the current context
     * @param item the friend who receives the game
     */
    fun buildGameList(context: Context, item: String){
        val builder = AlertDialog.Builder(context)
        val selectedItems = ArrayList<Int>() // Where we track the selected items
        val array = Array(games.size) {
            games[it]
        }
        // Get game data from current user and populate game array with correct data
        builder.setTitle("Game to send:")
            .setItems(array,
                DialogInterface.OnClickListener { dialog, which ->
                    // Updates database
                    System.out.println("Send " + item + " the game: " + games.get(which))
                    val activity = activity as MainActivity?
                    var gamestate: ArrayList<Int>;
                    try {
                        db.collection("users").document(activity!!.getGlobalUser().getEmail()).collection("games").document(
                            games.get(which)
                        )
                            .get()
                            .addOnSuccessListener { document ->
                                println(games.get(which))
                                activity.printState(activity.convertFirebaseToGamestate(document.data?.get("gamestate") as ArrayList<Int>))
                                gamestate = document.data?.get("gamestate") as ArrayList<Int>
                                val gameData = hashMapOf(
                                    "date" to games.get(which),
                                    "gamestate" to gamestate
                                )
                                try {
                                    db.collection("users").document(item).collection("games").document(games.get(which))
                                        .set(gameData)
                                        .addOnSuccessListener { Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!") }
                                        .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error writing document", e) }

                                }
                                catch (e: IllegalArgumentException){
                                    println("No game available.")
                                }

                            }
                            .addOnFailureListener { exception ->
                                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                            }
                    }
                    catch (e: IllegalArgumentException){
                        println(e)
                    }


//                    activity.printState(activity.convertFirebaseToGamestate(gamestate as ArrayList<Int>))
//                    println(gamestate)
                    // The 'which' argument contains the index position
                    // of the selected item
                })

        val dialog = builder.create()
        dialog.show()
    }
}