package sse.goethe.arsudoku
/**
 * Implements a User class that holds the information about the current User.
 *
 * @author Nils Gormsen
 * @param email the email of the user
 * @param name the name of the user
 */
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
