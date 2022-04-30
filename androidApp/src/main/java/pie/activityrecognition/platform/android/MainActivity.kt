package pie.activityrecognition.platform.android


import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import pie.activityrecognition.platform.Falls
import pie.activityrecognition.platform.Stumbles
import java.lang.Exception


// Alternative lower level Sensor class from Kotlin instead of google's
// activity recognition API -> https://developer.android.com/reference/kotlin/android/hardware/Sensor

fun stumbles(): String {
    return Stumbles().getStumbles()
}

fun falls(): String {
    return Falls().getFalls()
}
 // TODO - deprecate the google activity recognition api

class MainActivity : AppCompatActivity(), SensorEventListener {

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSensorChanged(event: SensorEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS),0)
            }

            //if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            //if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                    //ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),1)
            //        ActivityCompat.requestPermissions(this, arrayOf("com.google.android.gms.permission.ACTIVITY_RECOGNITION"),1)
            //}

            val stumbleCounter: TextView = findViewById(R.id.stumbleCounter)
            val fallCounter: TextView = findViewById(R.id.fallCounter)



            stumbleCounter.text = String.format("Stumble Counter: ", stumbles())
            fallCounter.text = String.format("Stumble Counter: ", falls())

            //val a = SensorService()


        } catch (e: Exception) {
            println(e)
        }
    }

}


