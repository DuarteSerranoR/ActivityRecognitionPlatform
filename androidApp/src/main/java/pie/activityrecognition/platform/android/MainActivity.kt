package pie.activityrecognition.platform.android


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.lang.Exception


// Alternative lower level Sensor class from Kotlin instead of google's
// activity recognition API -> https://developer.android.com/reference/kotlin/android/hardware/Sensor

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS),0)
            }

            //if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, "com.google.android.gms.permission.ACTIVITY_RECOGNITION") != PackageManager.PERMISSION_GRANTED) {
                    //ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),1)
                    ActivityCompat.requestPermissions(this, arrayOf("com.google.android.gms.permission.ACTIVITY_RECOGNITION"),1)
            }

            //val smsAct = SMSActivity(this.applicationContext)
            //smsAct.sendSMS("934786267", "Testing")

            val transitions = mutableListOf<ActivityTransition>()

            transitions +=
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.TILTING)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build()

            /*
            transitions +=
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.RUNNING)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    .build()

            transitions +=
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.WALKING)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    .build()
            */
        } catch (e: Exception) {
            println(e)
        }
    }

}


