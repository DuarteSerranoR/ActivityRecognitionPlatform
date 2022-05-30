package pie.activityrecognition.platform.android

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel: ViewModel() {

    private var mSensorsBinder: MutableLiveData<ActivityRecognitionSensors.LocalBinder> = MutableLiveData(ActivityRecognitionSensors().LocalBinder())
    private var mBound = false

    private val mConnection = object: ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, iBinder: IBinder) {
            val binder: ActivityRecognitionSensors.LocalBinder = iBinder as ActivityRecognitionSensors.LocalBinder
            mSensorsBinder.postValue(binder)
            mBound = true
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            mSensorsBinder.postValue(null)
            mBound = false
        }
    }

    fun getSensorServiceConnection() : ServiceConnection {
        return mConnection
    }

    fun getBinder() : MutableLiveData<ActivityRecognitionSensors.LocalBinder> {
        return mSensorsBinder
    }

}