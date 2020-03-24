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
    }

    fun removeSudokuNumber(row: Int, column: Int){
        currentState[row][column] = 0
        addCurrentStateToHistory()
    }

    fun setSudokuNumber(row: Int, column: Int, value: Int){
        currentState[row][column] = value
        addCurrentStateToHistory()
    }

    fun getCurrentState(): Array<IntArray>{
        return currentState
    }

    fun setHint(){
            val hint = sudoku.hint(currentState)
            println(hint)
            setSudokuNumber(hint.first, hint.second, hint.third )
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
            historyOfStates[historyPointer] = createDeepStateClone(currentState)

        }
        catch (e : IndexOutOfBoundsException){
            historyOfStates.add(createDeepStateClone(currentState))

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