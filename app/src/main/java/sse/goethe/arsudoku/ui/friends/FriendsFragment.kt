package sse.goethe.arsudoku.ui.friends

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
        val textView: TextView = root.findViewById(R.id.text_friends)
        friendsViewModel.text.observe(this, Observer {
            textView.text = it
        })
        val listView : SwipeMenuListView = root.findViewById(R.id.swipeMenu)
        val db = FirebaseFirestore.getInstance()
        val adapter = ArrayAdapter(root.context, android.R.layout.simple_list_item_1, users)
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
        }          // set creator

        listView.setMenuCreator(creator)
        listView.setAdapter(adapter)

        // Set database listener for friend data
        db.collection("users")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w("fail", "Listen failed.", e)
                    return@addSnapshotListener
                }
                users.clear()

                for (doc in value!!) {
                    doc.getString("first")?.let {
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
                        Log.d("succes", "onMenuItemClick: clicked item " + index)
                        users.add("element")
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