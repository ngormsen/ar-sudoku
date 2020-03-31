package sse.goethe.arsudoku

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
/**
 * Implements a Game class that holds the information about a scanned game.
 *
 * @author Nils Gormsen
 * @param email the email of the user that scanned the game
 * @param sudoku a Sudoku object
 */

@RequiresApi(Build.VERSION_CODES.O)
class Game (email: String, sudoku: Sudoku){
    private lateinit var date:  String
    private val email: String = email
    private val gamestate: Gamestate = Gamestate(sudoku)



    init {
        date = getDate()
    }

    /**
     * Sets the current time and date in an appropriate format.
     * @return a string with the current time and date.
     */
    fun getDate() : String{
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd|MM|yyyy HH:mm:ss")
        val formatted = current.format(formatter)
        return formatted
    }

    fun getGamestate(): Gamestate{
        return gamestate
    }

    fun getEmail(): String{
        return email
    }



}
