package sse.goethe.arsudoku.ui.friends

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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.baoyz.swipemenulistview.SwipeMenu
import com.baoyz.swipemenulistview.SwipeMenuCreator
import com.baoyz.swipemenulistview.SwipeMenuItem
import com.baoyz.swipemenulistview.SwipeMenuListView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import sse.goethe.arsudoku.MainActivity
import sse.goethe.arsudoku.R

class FriendsFragment : Fragment() {

    private lateinit var friendsViewModel: FriendsViewModel
    private val users = ArrayList<String>()  // Transfer to viewModel in production version

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

//            // Creates dialog fragment to add new friends
//            val dialogFragment = FriendDialog()
//
//            val ft = (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
//            val prev = (activity as AppCompatActivity).supportFragmentManager.findFragmentByTag("dialog")
//            if (prev != null)
//            {
//                ft.remove(prev)
//            }
//            ft.addToBackStack(null)
//            dialogFragment.show(ft, "dialog")
//
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
        }

//
//        val textView: TextView = root.findViewById(R.id.text_friends)
//        friendsViewModel.text.observe(this, Observer {
//            textView.text = it
//        })
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
            // set item background
            openItem.background = ColorDrawable(
                Color.rgb(
                    0xF9, 0x3F,
                    0x25
                )
            )
            // set item width
            openItem.width = 170
            // set item title
            openItem.title = "Delete"
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
                    75,
                    219, 87
                )
            )
            // set item width
            deleteItem.width = 170
            // set a icon
            deleteItem.setIcon(R.drawable.ic_menu_send)
            // add to menu
            menu.addMenuItem(deleteItem)
        }          // set creator

        // Attaches the Creator and Adapter to the Swipe Menu
        listView.setMenuCreator(creator)
        listView.setAdapter(adapter)

        // Access the MainActivity
        val activity = activity as MainActivity?

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
                         adapter.notifyDataSetChanged()  // Results in a bug that prevents menu from closing

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