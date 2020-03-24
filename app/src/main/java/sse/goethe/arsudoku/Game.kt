package sse.goethe.arsudoku

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class Game (email: String, sudoku: Sudoku){
    private lateinit var date:  String
    private val email: String = email
    private val gamestate: Gamestate = Gamestate(sudoku)



    init {
        date = getDate()
    }

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
        /**
         * Returns the email address of the user
         */
        return email
    }



}
