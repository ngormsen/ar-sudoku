package sse.goethe.arsudoku.ui.tools

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import sse.goethe.arsudoku.R

class ToolsFragment : Fragment() {

    private lateinit var toolsViewModel: ToolsViewModel
    var sudoku = arrayOf(
        intArrayOf(3, 0, 6, 5, 0, 8, 4, 0, 0),
        intArrayOf(5, 2, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(0, 8, 7, 0, 0, 0, 0, 3, 1),
        intArrayOf(0, 0, 3, 0, 1, 0, 0, 8, 0),
        intArrayOf(9, 0, 0, 8, 6, 3, 0, 0, 5),
        intArrayOf(0, 5, 0, 0, 9, 0, 6, 0, 0),
        intArrayOf(1, 3, 0, 0, 0, 0, 2, 5, 0),
        intArrayOf(0, 0, 0, 0, 0, 0, 0, 7, 4),
        intArrayOf(0, 0, 5, 2, 0, 6, 3, 0, 0)
    )

    fun setSudokuNumber(row: Int, column: Int, number: Int) {
        sudoku[row - 1][column - 1] = number
    }
    fun removeSudokuNumber(row: Int, column: Int){
        sudoku[row - 1][column - 1] = 0
    }
    fun checkSudokuNumber(row: Int, column: Int, number: Int){
        return
    }

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
        val lowerTableRow: TableLayout = root.findViewById(R.id.table_layout_row_2)
        var lastFieldSelected = "Default"
        var selectedField = ""
        // Set new table row layout parameters.
//        val layoutParams: TableRow.LayoutParams =
//            TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT)
//        tableRow.setLayoutParams(layoutParams)

//        // Add a TextView in the first column.

        // Define Background
        val sd = ShapeDrawable()
        // Specify the shape of ShapeDrawable
        sd.shape = RectShape()
        // Specify the border color of shape
        sd.paint.color = Color.BLACK
        // Set the border width
        sd.paint.strokeWidth = 10f
        // Specify the style is a Stroke
        sd.paint.style = Paint.Style.STROKE
        // Finally, add the drawable background to TextView

        for (row in 1..9){ // We need to start with 1 as we set the id to row and col (row would otherwise be zero)
            val tableRow = TableRow(context)
            for (column in 1..9){
                val textView = TextView(context)
                val displayNumber = "$row" + "$column"
                if (sudoku[row-1][column-1] != 0){
                    textView.text = sudoku[row-1][column-1].toString()
                }
                else{
                    textView.text = ""
                }
                textView.width = 110
                textView.height = 110
                textView.textSize = 30F
                textView.gravity = 11
                textView.id = displayNumber.toInt() // TODO umbauen zu string
                textView.setOnClickListener(View.OnClickListener {
                    // Click field
                    // set selectedField to current field
                    // set the current field to blue
                    // if we choose a new field, set the old one to white
                    // if the old field ist default, set the value for the old one to the current one

                    selectedField = displayNumber
                    if (selectedField != lastFieldSelected){
                        textView.setBackgroundColor(Color.LTGRAY)
                        if (lastFieldSelected == "Default"){
                            lastFieldSelected = 12.toString()
                        }
                        val lastTextField = root.findViewById<TextView>(lastFieldSelected.toInt())
                        lastTextField.setBackground(sd)
                        lastFieldSelected = displayNumber
                    }
                })

                textView.setBackground(sd)

                tableRow.addView(textView)
            }
            tableRow.gravity = 11
            upperTableRow.addView(tableRow)



        }


        // Create row for input buttons
        val tableRow = TableRow(context)
        for(column in 0..8){
            val textView = TextView(context)
            textView.text = (column+1).toString()
            textView.width = 110
            textView.height = 110
            textView.textSize = 30F
            textView.gravity = 11
            textView.setOnClickListener(View.OnClickListener {
                if(selectedField != ""){
                    val currentTextField = root.findViewById<TextView>(selectedField.toInt())
                    currentTextField.setText(textView.text)
                    val textFieldRow = currentTextField.id.toString()[0]
                    val textFieldColumn = currentTextField.id.toString()[1]
                    println(textFieldRow)
                    println(textFieldColumn)
                    setSudokuNumber(Integer.parseInt(textFieldRow.toString()), Integer.parseInt(textFieldColumn.toString()), Integer.parseInt(
                        textView.text as String
                    ))
                }
            })

            // Finally, add the drawable background to TextView
//            textView.setBackground(sd)
            textView.setBackgroundResource(R.drawable.input_number_bg)


            tableRow.addView(textView)
        }
        tableRow.gravity = 11
        lowerTableRow.addView(tableRow)



//        val twoOne = root.findViewById<TextView>(12)
//        println(twoOne.setText("5"))
        return root
    }

}



//        val textView: TextView = root.findViewById(R.id.text_tools)
//        toolsViewModel.text.observe(this, Observer {
//            textView.text = it
//        })
