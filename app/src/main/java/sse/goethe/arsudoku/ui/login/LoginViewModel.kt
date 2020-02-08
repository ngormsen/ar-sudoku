package sse.goethe.arsudoku.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.protobuf.ListValue
import sse.goethe.arsudoku.User

class LoginViewModel : ViewModel() {

    private val _user = MutableLiveData<User>().apply {
        value = User("NA", "NA")
    }

    private val _text = MutableLiveData<String>().apply {
        value = ""
    }

    val text: LiveData<String> = _text
    val user:LiveData<User> = _user


    fun setUser(user: User){
        _user.apply {
            value=user
        }
    }

}
//System.out.println("User data:"+ activity!!.getGlobalUser().getName())