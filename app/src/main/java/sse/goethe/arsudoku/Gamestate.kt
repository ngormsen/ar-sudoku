package sse.goethe.arsudoku

class Gamestate (private var currentState: Sudoku){

    private lateinit var solvedState: Sudoku
    private lateinit var historyOfStates: MutableList<Sudoku>
    private var historyPointer = 0

    init {
        // Add inital state to history
        historyOfStates.add(currentState)
        // Solve Sudoku and set solvedState
        currentState.solve()
        solvedState = Sudoku(currentState.getCurrentState())
    }

    //TODO If you input a new number you need to delete the future history from this point on
    fun addCurrentState(state: Sudoku){
        currentState = state
        historyPointer += 1
    }

    fun setCurrentState(state: Sudoku){
        historyOfStates.set(historyPointer, currentState)
        historyPointer += 1
    }

    fun getCurrentState(): Sudoku{
        return currentState
    }



    fun getSolvedState(): Sudoku{
        return solvedState
    }


    fun undo(){
        historyPointer -= 1
        setCurrentState(historyOfStates[historyPointer])
    }

    fun redo(){
        historyPointer += 1
        setCurrentState(historyOfStates[historyPointer])
    }

    fun solveAll(){
        currentState = solvedState
    }

    fun giveHint(){
//        currentState = currentState.hint()
    }


}