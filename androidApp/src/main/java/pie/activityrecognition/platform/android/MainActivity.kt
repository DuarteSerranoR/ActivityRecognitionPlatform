
package pie.activityrecognition.platform.android


import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import pie.activityrecognition.platform.android.activityapiservice.ActivityRecognitionService
import pie.activityrecognition.platform.android.sensorsservice.SensorsService
import java.lang.Exception


// Alternative lower level Sensor class from Kotlin instead of google's
// activity recognition API -> https://developer.android.com/reference/kotlin/android/hardware/Sensor

class MainActivity: AppCompatActivity() {


    /*
     * other ideas -> https://www.youtube.com/watch?v=fbj3c0LgXVI&t=97s -> downstairs, jogging, sitting, standing, upstairs, walking, biking
     *
     * (accel) running -> pet shakes or runs with you
     * (accel) walk -> likewise
     *
     * gravity -> pet is falling and will hit the ground hard. Or, if lighter, it is on an elevator
     * humidity -> if high for x time, it gets sick.
     * temperature -> too hot, sweats; too cold, trembles and shakes.
     * rainy weather with umbrella -> use humidity and pressure sensors. Research (Geography, meteorology).
     *
     * seat-belt -> fall detection?
     *
     * --------------------------------------
     *
     * running
     * (walking)
     * gravity 2x
     *
     * temperature -> too hot, sweats; too cold, trembles and shakes.
     * rainy weather with umbrella -> use humidity and pressure sensors. Research (Geography, meteorology).
     *
     * --------------------------------------
     *
     * TODO - sleeping detection - use sound and light
     *  compare the weather readings, ...., with real meteorology -> gps -> temperature
     *  gps, temperature and dry humidity -> beach
     *
     * */

    private lateinit var sensorServiceIntent: Intent
    private lateinit var activityRecognitionServiceIntent: Intent
    private lateinit var mSensorsService: SensorsService
    private lateinit var mActivityRecognitionService: ActivityRecognitionService
    private val scope = MainScope()
    override fun onStart() {
        super.onStart()
        // Bind to Service
        bindService(sensorServiceIntent, mSensorConnection, Context.BIND_AUTO_CREATE)
        bindService(activityRecognitionServiceIntent, mActivityRecognitionConnection, Context.BIND_AUTO_CREATE)
    }

    var mSensorBound = false
    private val mSensorConnection = object : ServiceConnection {
        // Called when the connection with the service is established
        @SuppressLint("SetTextI18n")
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // Because we have bound to an explicit
            // service that is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            val sensorsBinder = service as SensorsService.LocalBinder
            mSensorsService = sensorsBinder.getService()
            mSensorBound = true
            if (mSensorsService.running) { // TODO - do the same to the google api
                updateCurrentValues()
            } else {
                updateNaNValues()
            }
        }

        // Called when the connection with the service disconnects unexpectedly
        override fun onServiceDisconnected(className: ComponentName) {
            println("Disconnected SensorService")
            mSensorBound = false
        }
    }

    var mActivityRecognitionBound = false
    private val mActivityRecognitionConnection = object : ServiceConnection {
        // Called when the connection with the service is established
        @SuppressLint("SetTextI18n")
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // Because we have bound to an explicit
            // service that is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            val activityRecognitionBinder = service as ActivityRecognitionService.LocalBinder
            mActivityRecognitionService = activityRecognitionBinder.getService()
            mActivityRecognitionBound = true
            // TODO - do the same to the google api (get the values)
        }

        // Called when the connection with the service disconnects unexpectedly
        override fun onServiceDisconnected(className: ComponentName) {
            println("Disconnected ActivityRecognitionService")
            mActivityRecognitionBound = false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateNaNValues() {
        angleTxt.text = "Angle: NaN"
        directionTxt.text = "Direction: NaN"
        shakeTxt.text = "Shake: NaN"
        temperatureTxt.text = "Temperature: NaN"
        weatherTxt.text = "Weather: NaN"
        sickTxt.text = "Sick: NaN"
        lightTxt.text = "Light: NaN"
        sleepTxt.text = "Sleep: NaN"
        //soundTxt.text = "Sound: NaN"

        //runTxt.text = "Running: NaN"
        //runPTxt.text = "Running Percentage: NaN"
        //walkTxt.text = "Walking: NaN"
        //walkPTxt.text = "Walking Percentage: NaN"
    }

    @SuppressLint("SetTextI18n")
    private fun updateCurrentValues() {
        if (mSensorsService.hasMagnetometerSensor && mSensorsService.hasAccelerometerSensor)
            angleTxt.text = "Angle: " + mSensorsService.angle + "º"
        else
            angleTxt.text = "Sensors: Magnetometer '" + mSensorsService.hasMagnetometerSensor + "'; "+
                    "Accelerometer '" + mSensorsService.hasAccelerometerSensor + "'."

        if (mSensorsService.hasMagnetometerSensor && mSensorsService.hasAccelerometerSensor)
            directionTxt.text = "Direction: " + mSensorsService.direction
        else
            directionTxt.text = "Sensors: Magnetometer '" + mSensorsService.hasMagnetometerSensor + "'; "+
                    "Accelerometer '" + mSensorsService.hasAccelerometerSensor + "'."

        if (mSensorsService.hasAccelerometerSensor)
            shakeTxt.text = "Shake: " + mSensorsService.shake
        else
            shakeTxt.text = "No Accelerometer sensor was found on your device."

        if (mSensorsService.hasAmbientTemperatureSensor)
            temperatureTxt.text = "Temperature: " + mSensorsService.temperatureStatus
        else
            temperatureTxt.text = "No Temperature Sensor."

        if (mSensorsService.hasAmbientTemperatureSensor && mSensorsService.hasRelHumiditySensor)
            weatherTxt.text = "Weather: " + mSensorsService.weather
        else
            weatherTxt.text = "Sensors: AmbientTemperature '" + mSensorsService.hasAmbientTemperatureSensor + "'; "+
                    "RelativeHumidity '" + mSensorsService.hasRelHumiditySensor + "'."

        if (mSensorsService.hasAmbientTemperatureSensor && mSensorsService.hasRelHumiditySensor)
            sickTxt.text = "Sick: " + mSensorsService.sick
        else
            sickTxt.text = "Sensors: AmbientTemperature '" + mSensorsService.hasAmbientTemperatureSensor + "'; "+
                    "RelativeHumidity '" + mSensorsService.hasRelHumiditySensor + "'."

        if (mSensorsService.hasLightSensor)
            lightTxt.text = "Light: " + mSensorsService.light
        else
            lightTxt.text = "No Light Sensors detected."

        if (mSensorsService.hasLightSensor)
            sleepTxt.text = "Sleep: The target is " + mSensorsService.sleepStatus + "."
        else
            sleepTxt.text = "No Light Sensors detected."

        //if (mSensorsService.hasLightSensor)
        //    soundTxt.text = "Sound: " + mSensorsService.sound + ""
        //else
        //    soundTxt.text = "No Light Sensors detected, we skip sound."

        //runTxt.text = "Running: "
        //runPTxt.text = "Running Percentage: "
        //walkTxt.text = "Walking: "
        //walkPTxt.text = "Walking Percentage: "
    }

    override fun onStop() {
        super.onStop()
        unbindService(mSensorConnection)
    }

    private lateinit var directionTxt: TextView
    private lateinit var sickTxt: TextView
    private lateinit var angleTxt: TextView
    private lateinit var temperatureTxt: TextView
    private lateinit var weatherTxt: TextView
    private lateinit var shakeTxt: TextView
    private lateinit var lightTxt: TextView
    private lateinit var sleepTxt: TextView
    //private lateinit var soundTxt: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /* // TODO - this requires a valid policy url to justify the use of audio recording
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            val permissions =
                arrayOf(Manifest.permission.RECORD_AUDIO)

            ActivityCompat.requestPermissions(this, permissions,0)
        }
        */
        // this.checkSelfPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")

        sensorServiceIntent = Intent(this, SensorsService::class.java)
        activityRecognitionServiceIntent = Intent(this, ActivityRecognitionService::class.java)

        // Start Services
        startService(sensorServiceIntent)
        startService(activityRecognitionServiceIntent)

        // Get UI
        angleTxt = findViewById(R.id.angleTxt)
        directionTxt = findViewById(R.id.directionTxt)
        shakeTxt = findViewById(R.id.shakeTxt)
        temperatureTxt = findViewById(R.id.temperatureTxt)
        weatherTxt = findViewById(R.id.weatherTxt)
        sickTxt = findViewById(R.id.sickTxt)
        lightTxt = findViewById(R.id.lightTxt)
        sleepTxt = findViewById(R.id.sleepTxt)
        //soundTxt = findViewById(R.id.soundTxt)

        updateNaNValues()

        try {
            // Listen to the variables - didn't know how to setup a listener in a right way,
            // so did a loop
            startUIUpdatesCoroutine()

        } catch (e: Exception) {
            println(e)
        }
    }

    @SuppressLint("SetTextI18n")
    fun startUIUpdatesCoroutine() = runBlocking {
        scope.launch {
            delay(1000L)
            while (true) {
                try {
                    delay(10L)
                    if (mSensorBound && mSensorsService.running) {
                        updateCurrentValues()
                    }
                    // TODO - do the google api
                } catch (e: Exception) {
                    println(e)
                }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun resumeReadings(view: View) {
        mSensorsService.resumeReading()
        updateCurrentValues()
    }

    @Suppress("UNUSED_PARAMETER")
    @SuppressLint("SetTextI18n")
    fun pauseReadings(view: View) {
        mSensorsService.pauseReading()
        updateNaNValues()
    }
}
