package sse.goethe.arsudoku

class Gamestate (currentState: Sudoku){
    private var currentState: Sudoku = currentState
    private lateinit var solvedState: Sudoku
    private val historyOfStates: List<Sudoku> = mutableListOf<Sudoku>(currentState)

    init {

    }

    fun setCurrentState(state: Sudoku){
        currentState = state
    }

    fun getCurrentState(): Sudoku{
        return currentState
    }

    fun getSolvedState(): Sudoku{
        return solvedState
    }

    fun undo(){

    }

    fun solveAll(){
        currentState = solvedState
    }

    fun giveHint(){
//        currentState = currentState.hint()
    }

    fun setHistory(){
    }

    fun getHistory(){

    }


}