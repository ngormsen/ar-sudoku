package sse.goethe.arsudoku.ui.tools

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import kotlinx.coroutines.selects.select
import org.w3c.dom.Text
import sse.goethe.arsudoku.R

class ToolsFragment : Fragment() {

    private lateinit var toolsViewModel: ToolsViewModel

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get TableLayout object in layout xml.


        toolsViewModel =
            ViewModelProviders.of(this).get(ToolsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_tools, container, false)
        val tableLayout: TableLayout = root.findViewById(R.id.table_layout_table);
        val upperTableRow: TableLayout = root.findViewById(R.id.table_layout_row_1);
        val lowerTableRow: TableRow = root.findViewById(R.id.table_layout_row_2)
        var lastFieldSelected = ""
        var selectedField = ""
        // Set new table row layout parameters.
//        val layoutParams: TableRow.LayoutParams =
//            TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT)
//        tableRow.setLayoutParams(layoutParams)

//        // Add a TextView in the first column.
        for (row in 0..8){
            val tableRow = TableRow(context)
            for (column in 0..8){
                val textView = TextView(context)
                val displayNumber = "$row" + "$column"
                textView.text = displayNumber
                textView.width = 110
                textView.height = 110
                textView.textSize = 30F
                textView.gravity = 11
                textView.id = displayNumber.toInt()
                textView.setOnClickListener(View.OnClickListener {
                    // Click field
                    // set selectedField to current field
                    // set the current field to blue
                    // if we choose a new field, set the old one to white
                    // if the old field ist default, set the value for the old one to the current one

                    selectedField = displayNumber
                    if (selectedField != lastFieldSelected){
                         if (lastFieldSelected == ""){
                             lastFieldSelected = displayNumber
                         }

                        textView.setBackgroundColor(Color.RED)
                        val lastTextField = root.findViewById<TextView>(lastFieldSelected.toInt())
                        lastTextField.setBackgroundColor(Color.WHITE)
                        lastFieldSelected = displayNumber

                    }
                })


                tableRow.addView(textView)
            }
            tableRow.gravity = 11
            upperTableRow.addView(tableRow)



        }
        for (row in 1..2){
            
        }
//        val twoOne = root.findViewById<TextView>(12)
//        println(twoOne.setText("5"))
        return root
    }
}


//        val textView: TextView = root.findViewById(R.id.text_tools)
//        toolsViewModel.text.observe(this, Observer {
//            textView.text = it
//        })
