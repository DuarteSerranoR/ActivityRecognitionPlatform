package pie.activityrecognition.platform.android


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
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
    companion object commonMain {
        init {
            System.loadLibrary("commonMain")
        }
        external fun fall_detection_algorithm(accel1: Float, accel2: Float, accel3: Float): Boolean
    }

    private lateinit var mSensorManager : SensorManager
    private var mAccelerometer : Sensor ?= null
    private var resume = false;

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onSensorChanged(event: SensorEvent?) { // https://mdpi-res.com/d_attachment/sensors/sensors-15-17827/article_deploy/sensors-15-17827-v2.pdf?version=1438068266
        if (event != null && resume) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            //if (event.sensor.type == Sensor.TYPE_ACCELEROMETER_UNCALIBRATED) {
                val accelVal = event.values.asList().toString()
                findViewById<TextView>(R.id.sensor_value).text = "Acceleration: " + accelVal
                val fell = commonMain.fall_detection_algorithm(event.values[0], event.values[1], event.values[2])
                print(fell)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
    }

    fun resumeReading(view: View) {
        this.resume = true
    }

    fun pauseReading(view: View) {
        this.resume = false
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

            mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

            //val a = SensorService()


        } catch (e: Exception) {
            println(e)
        }
    }

}


