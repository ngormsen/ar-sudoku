package sse.goethe.arsudoku

import java.lang.IndexOutOfBoundsException
import kotlin.properties.Delegates

/**
 * Implements a Gamestate class that manages the current and solved state of a given Sudoku.
 * Implements a history for using undo and redo when playing the game.
 *
 * @author Nils Gormsen
 * @param sudoku a given sudoku object
 */

class Gamestate (sudoku: Sudoku){

    private var sudoku: Sudoku = sudoku
    private var solvedState: Array<IntArray> = arrayOf(
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
    private var visualizeSate: Array<IntArray> = arrayOf(
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

    private lateinit var currentState: Array<IntArray>
    private var historyOfStates: MutableList<Array<IntArray>> = arrayListOf()
    private var historyPointer: Int = 0
    private var solvableState = true;

    init {
        // Setting current state to inital state
        currentState = createDeepStateClone(sudoku.getCurrentState())
        // Solve sudoku
        sudoku.solve()
        solvableState = sudoku.getSolvableState();
        if(solvableState){
            solvedState = createDeepStateClone(sudoku.getCurrentState())
            // Add inital state to history
            historyOfStates.add(createDeepStateClone(currentState))
            visualizeSate = createVisualizeState(currentState, solvedState);
        }
    }

    fun getSolvable(): Boolean{
        return solvableState;
    };

    fun getVisualizeState(): Array<IntArray>{
        return visualizeSate;
    };

    fun createVisualizeState(unsolvedState: Array<IntArray>, solvedState: Array<IntArray>): Array<IntArray>{
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
        var listIdx = 0
        for (row in 0..8) {
            for (column in 0..8) {
                if (unsolvedState[row][column] != solvedState[row][column]){
                    newState[row][column] = solvedState[row][column]
                }
                listIdx += 1
            }
        }
        return newState
    }

    /**
     * Removes a given number and updates the history.
     * @param row given row of the Sudoku
     * @param column given column of the Sudoku
     */
    fun removeSudokuNumber(row: Int, column: Int){
        currentState[row][column] = 0
        addCurrentStateToHistory()
    }

    /**
     * Adds a value to the current state.
     * Updates history.
     */
    fun setSudokuNumber(row: Int, column: Int, value: Int){
        currentState[row][column] = value
        addCurrentStateToHistory()
    }

    fun getCurrentState(): Array<IntArray>{
        return currentState
    }

    /**
     * Receives and sets a possible hint from the Sudoku class to the current state.
     */
    fun setHint(){
            val hint = sudoku.hint(currentState)
            try {
                setSudokuNumber(hint.first, hint.second, hint.third )
            }
            catch (e: IndexOutOfBoundsException){
                println("No more hints available.")
            }
    }

    fun getStateHistory():MutableList<Array<IntArray>>{
        return historyOfStates
    }


    fun getSolvedState(): Array<IntArray>{
        return solvedState
    }

    /**
     * Adds a deep copy of the current state to the history.
     */
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
    /**
     * Changes the pointer and sets the current state to the previous state in the history.
     */
    fun undo(){
        try {
            currentState = createDeepStateClone(historyOfStates[historyPointer-1])
            historyPointer -= 1
        }
        catch (e: IndexOutOfBoundsException){
            println("Index out of bounds")
        }
    }

    /**
     * Changes the pointer and sets the current state to the next state in the history.
     */
    fun redo(){
        try{
            currentState = createDeepStateClone(historyOfStates[historyPointer+1])
            historyPointer += 1
        }
        catch (e: IndexOutOfBoundsException){
            println("Index out of bounds")
        }
    }
    /**
     * Creates a deep copy instead of a simple reference pass of the Sudoku state.
     * @param currentState the given Sudoku state
     * @return a copy of the given state
     */
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