package sse.goethe.arsudoku

import java.lang.IndexOutOfBoundsException

class Gamestate (sudoku: Sudoku){

    private var sudoku: Sudoku = sudoku
    private lateinit var solvedState: Array<IntArray>
    private lateinit var currentState: Array<IntArray>
    private var historyOfStates: MutableList<Array<IntArray>> = arrayListOf()
    private var historyPointer: Int = 0

    init {
        // Setting current state to inital state
        currentState = createDeepStateClone(sudoku.getCurrentState())
        // Solve sudoku
        sudoku.solve()
        solvedState = createDeepStateClone(sudoku.getCurrentState())
        // Add inital state to history
        historyOfStates.add(createDeepStateClone(currentState))
        println("added current state to history of states")
        println("current state:")
        println(currentState)
        println("solved state:")
        println(solvedState)
        println("inital history state:")
        println(historyOfStates[0])
        println(historyOfStates.size)
    }

    fun setSudokuNumber(row: Int, column: Int, value: Int){
        currentState[row][column] = value
        addCurrentStateToHistory()
    }

    fun getCurrentState(): Array<IntArray>{
        return currentState
    }

    fun setHint(){
//        try {
            val hint = sudoku.hint(currentState)
            println(hint)
            setSudokuNumber(hint.first, hint.second, hint.third )
//        }
//        catch (e: IndexOutOfBoundsException){
//            println("No more hints available.")
//        }
    }

    fun getStateHistory():MutableList<Array<IntArray>>{
        return historyOfStates
    }

    fun getSolvedState(): Array<IntArray>{
        return sudoku.getCurrentState()
    }

    fun addCurrentStateToHistory(){
        historyPointer += 1
        print(historyPointer)
        try{
            historyOfStates[historyPointer].isEmpty()
            println("set")
            historyOfStates[historyPointer] = createDeepStateClone(currentState)

        }
        catch (e : IndexOutOfBoundsException){
            println("add")
            historyOfStates.add(createDeepStateClone(currentState))

        }
        println("pointer:")
        println(historyPointer)
        println("historysize:")
        println(historyOfStates.size)



        println("new nurrent state added to history")
        println("history:")
        for (state in historyOfStates){
            println(state)
        }
    }

    fun undo(){
        try {
            currentState = createDeepStateClone(historyOfStates[historyPointer-1])
            historyPointer -= 1

        }
        catch (e: IndexOutOfBoundsException){
            println("Index out of bounds")
        }
    }

    fun checkSudokuNumber(i: Int, j: Int, x: Int): Boolean{
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

    fun redo(){
        try{
            currentState = createDeepStateClone(historyOfStates[historyPointer+1])
            historyPointer += 1

        }
        catch (e: IndexOutOfBoundsException){
            println("Index out of bounds")
        }
    }

    fun createDeepStateClone(currentState: Array<IntArray>): Array<IntArray> {
        var newState = arrayOf(
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0)
        )
        for (row in 0..8) {
            for (column in 0..8) {
                newState[row].set(column, currentState[row][column])
            }
        }
        return newState
    }


}