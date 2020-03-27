package sse.goethe.arsudoku.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import sse.goethe.arsudoku.MainActivity
import sse.goethe.arsudoku.R

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val activity = activity as MainActivity



        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(this, Observer {
            textView.text = it
        })

// get reference to button
        val btn_click_me = root.findViewById(R.id.testbutton) as Button
// set on-click listener
        btn_click_me.setOnClickListener {
            Toast.makeText(this.context, "Camera stopped....", Toast.LENGTH_SHORT).show()
            (activity as MainActivity).stopCamera()

        }
        println("Click on Test Button")



    return root
    }
}