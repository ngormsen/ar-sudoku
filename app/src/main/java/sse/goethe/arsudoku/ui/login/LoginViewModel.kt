package sse.goethe.arsudoku.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = ""
    }

    private val _userEmail = MutableLiveData<String>().apply{
        value = ""
    }

    private val _userName = MutableLiveData<String>().apply {
        value = ""
    }
    val text: LiveData<String> = _text
    val userEmail :LiveData<String> = _userEmail
    val userName: LiveData<String> = _userName

    fun setUserName(name: String){
        _userName.apply {
            value = name
        }
    }

    fun setUserEmail(email: String){
        _userEmail.apply {
            value = email
        }
    }

}