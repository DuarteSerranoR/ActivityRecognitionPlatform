package pie.activity_recognition.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    private val _activeServiceBool = MutableLiveData<Boolean>().apply {
        value = false
    }

    var activeServiceBool: LiveData<Boolean> = _activeServiceBool
}