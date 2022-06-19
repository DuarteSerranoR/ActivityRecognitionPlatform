package pie.activity_recognition.ui.missing_sensors

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MissingSensorsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Not Initialized"
    }
    val text: LiveData<String> = _text
}