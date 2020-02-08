package sse.goethe.arsudoku

class User (name: String, email: String){
    private val name: String = name
    private val email: String = email


    fun getName() : String{
        return name
    }

    fun getEmail(): String{
        return email
    }
}
