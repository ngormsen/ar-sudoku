package sse.goethe.arsudoku

import java.util.*

class Game (date: Date, email: String, sudoku: Sudoku){
    private val date:  Date = date
    private val email: String = email
    private val gamestate: Gamestate = Gamestate(sudoku)

    fun getDate() : String{
        return date.toString()
    }

    fun getGamestate(): Gamestate{
        return gamestate
    }

    fun getEmail(): String{
        /**
         * Returns the email address of the user
         */
        return email
    }



}
