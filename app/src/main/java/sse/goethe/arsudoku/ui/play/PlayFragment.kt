package sse.goethe.arsudoku.ui.play

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import sse.goethe.arsudoku.Gamestate
import sse.goethe.arsudoku.MainActivity
import sse.goethe.arsudoku.R
import java.lang.Integer.parseInt
/**
 * Implements a fragment that allows the user to play the game. The user can choose
 * to play a certain game from his history or just play the most recently scanned game.
 * The visualisation fo the Sudoku is generated dynamically.
 *
 * @author Nils Gormsen
 */
class PlayFragment : Fragment() {

    private lateinit var playViewModel: PlayViewModel
    private lateinit var gamestate: Gamestate
    private lateinit var currentState: Array<IntArray>

    /**
     * Helper function to print the current state for debugging purposes.
     */
    fun printCurrentState(){
        var n = 9
        for (i in 0 until n) {
            for (j in 0 until n) {
                print(currentState[i][j].toString())
                if (Math.floorMod(j, 3) == 2 && j < n - 1)
                    print(" ")
            }
            println()
            if (Math.floorMod(i, 3) == 2 && i < n - 1) println()
        }
    }

    /**
     * Calls the redo function from the gamestate object and updates the visualisations.
     */
    fun redo(view: View){
        gamestate.redo()
        currentState = gamestate.getCurrentState()
        updateSudokuVisualisation(view)

    }
    /**
     * Calls the undo function from the gamestate object and updates the visualisations.
     */
    fun undo(view: View){
        gamestate.undo()
        currentState = gamestate.getCurrentState()
        updateSudokuVisualisation(view)
    }
    /**
     * Calls the hint function from the gamestate object and updates the visualisations.
     */
    fun setHint(view: View){
        gamestate.setHint()
        updateSudokuVisualisation(view)

    }
    /**
     * Checks whether inputting the number on the choosen field is possible and sets the number.
     *
     * @param row row of the Sudoku
     * @param column column of the Sudoku
     * @param number number to set
     */
    fun setSudokuNumber(row: Int, column: Int, number: Int) {
        if(checkSudokuNumber(row, column, number)){
            gamestate.setSudokuNumber(row, column, number)
        }
        else{
            var toast = Toast.makeText(this.context, "Not a valid move!", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 200)
            toast.show()
        }
    }

    /**
     * Removes the Sudoku number given a row and a column.
     *
     * @param row row of the Sudoku
     * @param column column of the Sudoku
     */

    fun removeSudokuNumber(row: Int, column: Int){
        gamestate.removeSudokuNumber(row, column)
    }

    /**
     * Checks whether it is possible to input the number on the given field.
     *
     * @param i row of the Sudoku
     * @param j column of the Sudoku
     * @param x number
     */
    fun checkSudokuNumber(i: Int, j: Int, x: Int): Boolean {
        var n = 9
        // Is 'x' used in row.
        for (jj in 0 until n) {
            print(currentState[i][jj])
            if (currentState[i][jj] == x) {
                return false
            }
        }
        // Is 'x' used in column.
        for (ii in 0 until n) {
            if (currentState[ii][j] == x) {
                return false
            }
        }
        // Is 'x' used in sudoku 3x3 box.
        val boxRow = i - i % 3
        val boxColumn = j - j % 3
        for (ii in 0..2) {
            for (jj in 0..2) {
                if (currentState[boxRow + ii][boxColumn + jj] == x) {
                    return false
                }
            }
        }
        // Everything looks good.
        return true
    }

//    fun solveAll(){
//        return
//    }

    /**
     * Updates the visualisations of the Sudoku.
     *
     * @param view the current view
     */
    @SuppressLint("ResourceType")
    fun updateSudokuVisualisation(view: View){
        for (row in 1..9) { // We need to start with 1 as we set the id to row and col (row would otherwise be zero)
            for (column in 1..9) {
                var textView: TextView = view.findViewById<TextView>(parseInt("${row}${column}"))
//                println("text displayed: ${textView.text}")
//                println("value from sudoku: ${sudoku[row-1][column-1]}")
                if (currentState[row-1][column-1] != 0){
                    textView.text = "${currentState[row-1][column-1]}"
                }
                else{
                    textView.text = ""
                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Get current Gamestate from mainactivity
        val activity = activity as MainActivity
        if (activity != null) {
            activity.stopCamera()
        }

        gamestate = activity.getGame().getGamestate()
        currentState = gamestate.getCurrentState()
        playViewModel =
            ViewModelProviders.of(this).get(PlayViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_tools, container, false)
        val tableLayout: TableLayout = root.findViewById(R.id.table_layout_table);
        val upperTableRow: TableLayout = root.findViewById(R.id.table_layout_row_1);
        val lowerTableRow: TableLayout = root.findViewById(R.id.table_layout_row_2)
        var lastFieldSelected = "Default"
        var selectedField = ""

        // Create Sudoku Grid
        for (row in 1..9){ // We need to start with 1 as we set the id to row and col (row would otherwise be zero)
            val tableRow = TableRow(context)
            for (column in 1..9){
                val textView = TextView(context)
                val displayNumber = "$row" + "$column"
                if (currentState[row-1][column-1] != 0){
                    textView.text = currentState[row-1][column-1].toString()
                }
                else{
                    textView.text = ""
                }
                textView.width = 110
                textView.height = 110
                textView.textSize = 30F
                textView.gravity = 11
                textView.id = displayNumber.toInt() // TODO umbauen zu string
                textView.gravity = Gravity.CENTER_VERTICAL
                textView.gravity = Gravity.CENTER_HORIZONTAL
                textView.setOnClickListener(View.OnClickListener {

                    selectedField = displayNumber
                    if (selectedField != lastFieldSelected){
                        textView.setBackgroundResource(R.drawable.input_number_bg3)

                        if (lastFieldSelected == "Default"){
                            lastFieldSelected = 12.toString()
                        }
                        val lastTextField = root.findViewById<TextView>(lastFieldSelected.toInt())
                        var lastFieldSelectedRow = parseInt(lastFieldSelected[0].toString())
                        var lastFieldSelectedCol = parseInt(lastFieldSelected[1].toString())

                        if((lastFieldSelectedRow in 4..6 && lastFieldSelectedCol in 4..6) ||
                            (lastFieldSelectedRow in 1..3 || lastFieldSelectedRow in 7..9 )&&
                            (lastFieldSelectedCol in 1..3 || lastFieldSelectedCol in 7..9)) {
                            lastTextField.setBackgroundResource(R.drawable.input_number_bg2)
                        }else{
                            lastTextField.setBackgroundResource(R.drawable.input_number_bg)

                        }

                        lastFieldSelected = displayNumber
                    }
                })

//                textView.setBackground(sd)
                if((row in 4..6 && column in 4..6) || (row in 1..3 || row in 7..9 )&& (column in 1..3 || column in 7..9)){
                    textView.setBackgroundResource(R.drawable.input_number_bg2)
                }
                else{
                    textView.setBackgroundResource(R.drawable.input_number_bg)
                }


                tableRow.addView(textView)



            }
            tableRow.gravity = 11
            tableRow.setGravity(Gravity.CENTER_HORIZONTAL);
            upperTableRow.addView(tableRow)
            val v = View(getActivity())
        }

        // create row for delete,hint,undo,redo buttons
        var tableRowActions = TableRow(context)

        // Create hint button
        val textViewHint = TextView(context)
        textViewHint.text = "HINT"
        textViewHint.width = 110
        textViewHint.height = 110
        textViewHint.textSize = 20F
        textViewHint.gravity = Gravity.CENTER_VERTICAL
        textViewHint.gravity = Gravity.CENTER_HORIZONTAL
        //theChild in this case is the child of TableRow
        textViewHint.setOnClickListener(View.OnClickListener {
            setHint(root)
        })
        textViewHint.setBackgroundResource(R.drawable.input_number_bg)
        tableRowActions.addView(textViewHint)
        val paramsHint = textViewHint.layoutParams as TableRow.LayoutParams
        paramsHint.span = 2 //amount of columns you will span
        textViewHint.setLayoutParams(paramsHint)


        // Create undo button
        val textViewUndo = TextView(context)
        textViewUndo.text = "UNDO"
        textViewUndo.width = 110
        textViewUndo.height = 110
        textViewUndo.textSize = 20F

        textViewUndo.gravity = Gravity.CENTER_VERTICAL
        textViewUndo.gravity = Gravity.CENTER_HORIZONTAL
        //theChild in this case is the child of TableRow
        textViewUndo.setOnClickListener(View.OnClickListener {
            undo(root)
        })
        textViewUndo.setBackgroundResource(R.drawable.input_number_bg)
        tableRowActions.addView(textViewUndo)
        val paramsUndo = textViewUndo.layoutParams as TableRow.LayoutParams
        paramsUndo.span = 2 //amount of columns you will span
        textViewUndo.setLayoutParams(paramsUndo)

        // Create redo button
        val textViewRedo = TextView(context)
        textViewRedo.text = "REDO"
        textViewRedo.width = 110
        textViewRedo.height = 110
        textViewRedo.textSize = 20F
        textViewRedo.gravity = Gravity.CENTER_VERTICAL
        textViewRedo.gravity = Gravity.CENTER_HORIZONTAL
        //theChild in this case is the child of TableRow
        textViewRedo.setOnClickListener(View.OnClickListener {
            redo(root)
        })
        textViewRedo.setBackgroundResource(R.drawable.input_number_bg)
        tableRowActions.addView(textViewRedo)
        val paramsRedo = textViewRedo.layoutParams as TableRow.LayoutParams
        paramsRedo.span = 2 //amount of columns you will span
        textViewRedo.setLayoutParams(paramsRedo)

        // Create delete button
        val textViewDelete = TextView(context)
        textViewDelete.text = "DELETE"
        textViewDelete.width = 110
        textViewDelete.height = 110
        textViewDelete.textSize = 20F
        textViewDelete.gravity = Gravity.CENTER_VERTICAL
        textViewDelete.gravity = Gravity.CENTER_HORIZONTAL
        //theChild in this case is the child of TableRow
        textViewDelete.setOnClickListener(View.OnClickListener {
            if(selectedField != ""){
                val currentTextField = root.findViewById<TextView>(selectedField.toInt())
                val textFieldRow = currentTextField.id.toString()[0]
                val textFieldColumn = currentTextField.id.toString()[1]
                removeSudokuNumber(Integer.parseInt(textFieldRow.toString())-1, Integer.parseInt(textFieldColumn.toString())-1)
            }
            updateSudokuVisualisation(root)
        })
        textViewDelete.setBackgroundResource(R.drawable.input_number_bg)
        tableRowActions.addView(textViewDelete)
        val paramsDelete = textViewDelete.layoutParams as TableRow.LayoutParams
        paramsDelete.span = 2 //amount of columns you will span
        textViewDelete.setLayoutParams(paramsDelete)

        tableRowActions.setGravity(Gravity.CENTER_HORIZONTAL);

        // Add Row to layout
        lowerTableRow.addView(tableRowActions)

        // Create row for input buttons
        val tableRowNumbers = TableRow(context)
        for(column in 0..8){
            val textView = TextView(context)
            textView.text = (column+1).toString()
            textView.width = 110
            textView.height = 110
            textView.textSize = 30F
            textView.gravity = 11
            textView.gravity = Gravity.CENTER_VERTICAL
            textView.gravity = Gravity.CENTER_HORIZONTAL

            textView.setOnClickListener(View.OnClickListener {
                if(selectedField != ""){
                    val currentTextField = root.findViewById<TextView>(selectedField.toInt())
//                    currentTextField.setText(textView.text)
                    val textFieldRow = currentTextField.id.toString()[0]
                    val textFieldColumn = currentTextField.id.toString()[1]
                    println(textFieldRow)
                    println(textFieldColumn)
                    setSudokuNumber(Integer.parseInt(textFieldRow.toString())-1, Integer.parseInt(textFieldColumn.toString())-1, Integer.parseInt(
                        textView.text as String
                    ))
                }
                updateSudokuVisualisation(root)
            })

            // Finally, add the drawable background to TextView
            textView.setBackgroundResource(R.drawable.input_number_bg)
            tableRowNumbers.addView(textView)
        }
        tableRowNumbers.setGravity(Gravity.CENTER_HORIZONTAL);
        lowerTableRow.addView(tableRowNumbers)

        return root
    }

}



//        val textView: TextView = root.findViewById(R.id.text_tools)
//        toolsViewModel.text.observe(this, Observer {
//            textView.text = it
//        })

//
//private var sudoku = Sudoku(arrayOf(
//    intArrayOf(3, 0, 6, 5, 0, 8, 4, 0, 0),
//    intArrayOf(5, 2, 0, 0, 0, 0, 0, 0, 0),
//    intArrayOf(0, 8, 7, 0, 0, 0, 0, 3, 1),
//    intArrayOf(0, 0, 3, 0, 1, 0, 0, 8, 0),
//    intArrayOf(9, 0, 0, 8, 6, 3, 0, 0, 5),
//    intArrayOf(0, 5, 0, 0, 9, 0, 6, 0, 0),
//    intArrayOf(1, 3, 0, 0, 0, 0, 2, 5, 0),
//    intArrayOf(0, 0, 0, 0, 0, 0, 0, 7, 4),
//    intArrayOf(0, 0, 5, 2, 0, 6, 3, 0, 0)
//))