package sse.goethe.arsudoku

class Gamestate (sudoku: Sudoku){

    private var sudoku: Sudoku = sudoku
    private lateinit var currentState: Array<IntArray>
    private lateinit var historyOfStates: MutableList<Array<IntArray>>
    private var historyPointer = 0

    init {
        currentState = sudoku.getCurrentState()
        sudoku.solve()
        // Add inital state to history
        historyOfStates.add(currentState)
    }

    fun setCurrentState(state: Sudoku){
        historyOfStates.set(historyPointer, currentState)
        historyPointer += 1
    }

    fun getCurrentState(): Array<IntArray>{
        return currentState
    }



    fun getSolvedState(): Array<IntArray>{
        return sudoku.getCurrentState()
    }


    fun undo(){
        historyPointer -= 1
    }

    fun redo(){
        historyPointer += 1
    }

    fun solveAll(){
        return
    }

    fun giveHint(){
//        currentState = currentState.hint()
    }


}