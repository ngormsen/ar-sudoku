package sse.goethe.arsudoku

import java.util.*

class Game (date: Date, email: String){
    private val date:  Date = date
    private val email: String = email
    private val favorite: Boolean = false
//    private val gamestate: Gamestate = gamestate


    fun getDate() : String{
        return date.toString()
    }

    fun getEmail(): String{
        return email
    }
}
