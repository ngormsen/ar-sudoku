package sse.goethe.arsudoku

import java.util.*

class Game (date: Date, email: String){
    private val date:  Date = date
    private val email: String = email
    private var favorite: Boolean = false
//    private var gamestate: Gamestate =


    fun getDate() : String{
        return date.toString()
    }

    fun getEmail(): String{
        return email
    }

}
