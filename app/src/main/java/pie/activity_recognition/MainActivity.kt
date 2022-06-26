package pie.activity_recognition

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pie.activity_recognition.activityapi_service.ActivityRecognitionService
import pie.activity_recognition.databinding.ActivityMainBinding
import pie.activity_recognition.sensors_service.SensorsService
import java.lang.Exception


class MainActivity: AppCompatActivity() {

    // TODO - turn bound service into started service, or start the bound service from a
    //  always running started service that is always connected. So it doesn't die out
    //  until the started service dies.
    //  Take into consideration the permissions aspect... the started service needs to
    //  have the same MIC service as the Activity.


    /*
     * other ideas -> https://www.youtube.com/watch?v=fbj3c0LgXVI&t=97s -> downstairs, jogging,
     *                                              sitting, standing, upstairs, walking, biking
     *
     *
     *
     * seat-belt -> fall and stumble detection? -> lots of papers, few algorithms. ML?
     *
     *
     * TODO -
     *  GOOGLE API (running, walking, ...)
     *  compare the weather readings, ...., with real meteorology -> gps -> temperature
     *  gps, temperature and dry humidity -> beach
     *  HEALTH PLATFORM API (use sensors for health) ->
     *              https://developer.android.com/training/wearables/health-services/health-platform
     *  DEPRECATED TO HEALTH CONNECT ->
     *              https://developer.android.com/guide/health-and-fitness/health-connect
     *
     * TODO - Use the sensors to stipulate the energy consumed by the user, and make a
     *  hunger meter.
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
        bindService(activityRecognitionServiceIntent, mActivityRecognitionConnection,
            Context.BIND_AUTO_CREATE)
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
        angleTxt?.text = "Angle: NaN"
        directionTxt?.text = "Direction: NaN"
        shakeTxt?.text = "Shake: NaN"
        temperatureTxt?.text = "Temperature: NaN"
        weatherTxt?.text = "Weather: NaN"
        sickTxt?.text = "Sick: NaN"
        lightTxt?.text = "Light: NaN"
        sleepTxt?.text = "Sleep: NaN"
        soundTxt?.text = "Sound: NaN"

        //runTxt.text = "Running: NaN"
        //runPTxt.text = "Running Percentage: NaN"
        //walkTxt.text = "Walking: NaN"
        //walkPTxt.text = "Walking Percentage: NaN"
    }

    private fun updateMissingSensorsTxt(text: String) {
        missingSensorsHomeTxt?.text = text
        missingSensorsDashboardTxt?.text = text
        missingSensorsSensorsTxt?.text = text
    }

    private fun initMissingSensorsTxt() {
        missingSensorsHomeTxt = binding.root.findViewById(R.id.missing_sensors_home)
        missingSensorsDashboardTxt = binding.root.findViewById(R.id.missing_sensors_dashboard)
        missingSensorsSensorsTxt = binding.root.findViewById(R.id.missing_sensors_sensors)
    }

    @SuppressLint("SetTextI18n")
    private fun updateCurrentValues() {

        var missingSensorsVar = ""

        if (!(mSensorsService.hasAccelerometerSensor &&
                    mSensorsService.hasMagnetometerSensor &&
                    mSensorsService.hasAmbientTemperatureSensor &&
                    mSensorsService.hasRelHumiditySensor &&
                    mSensorsService.hasLightSensor &&
                    mSensorsService.audioPermissions)
        )
            missingSensorsVar = "Missing Sensors or Permissions:\n"

        if (!mSensorsService.hasAccelerometerSensor)
            missingSensorsVar += "\t- Accelerometer\n"
        if (!mSensorsService.hasMagnetometerSensor)
            missingSensorsVar += "\t- Magnetometer\n"
        if (!mSensorsService.hasAmbientTemperatureSensor)
            missingSensorsVar += "\t- Ambient Temperature\n"
        if (!mSensorsService.hasRelHumiditySensor)
            missingSensorsVar += "\t- Relative Humidity\n"
        if (!mSensorsService.hasLightSensor)
            missingSensorsVar += "\t- Light"
        if (!mSensorsService.audioPermissions)
            missingSensorsVar += "\t- Audio Permissions\n"

        updateMissingSensorsTxt(missingSensorsVar)



        if (mSensorsService.hasAccelerometerSensor)
            shakeTxt?.text = "Shake: " + mSensorsService.shake
        else
            shakeTxt?.text = "Missing sensors!"

        if (mSensorsService.hasAmbientTemperatureSensor)
            temperatureTxt?.text = "Temperature: " + mSensorsService.temperatureStatus
        else
            temperatureTxt?.text = "Missing sensors!"

        if (mSensorsService.hasRelHumiditySensor)
            weatherTxt?.text = "Weather: " + mSensorsService.weather
        else
            weatherTxt?.text = "Missing sensors!"

        if (mSensorsService.hasLightSensor)
            lightTxt?.text = "Light: " + mSensorsService.light + "lux"
        else
            lightTxt?.text = "Missing sensors!"

        if (mSensorsService.audioPermissions)
            soundTxt?.text = "Sound: " + mSensorsService.sound + "db"
        else
            soundTxt?.text = "Missing audio permission!"



        if (mSensorsService.hasMagnetometerSensor && mSensorsService.hasAccelerometerSensor) {
            angleTxt?.text = "Angle: " + mSensorsService.angle + "º"
            directionTxt?.text = "Direction: " + mSensorsService.direction
        } else {
            angleTxt?.text = "Missing sensors!"
            directionTxt?.text = "Missing sensors!"
        }

        if (mSensorsService.hasAmbientTemperatureSensor && mSensorsService.hasRelHumiditySensor)
            sickTxt?.text = "Sick: " + mSensorsService.sick
        else
            sickTxt?.text = "Missing sensors!"

        if (mSensorsService.hasLightSensor && mSensorsService.audioPermissions)
            sleepTxt?.text = "Sleep: The target is " + mSensorsService.sleepStatus + "."
        else
            sleepTxt?.text = "Missing sensor or\naudio permission!"

        //runTxt.text = "Running: "
        //runPTxt.text = "Running Percentage: "
        //walkTxt.text = "Walking: "
        //walkPTxt.text = "Walking Percentage: "

    }

    override fun onStop() {
        super.onStop()
        unbindService(mSensorConnection)
    }

    private var statusCheckBox: CheckBox? = null
    private var missingSensorsHomeTxt: TextView? = null // TODO - find a way to create a
                                                        //  shared component!!
    private var missingSensorsDashboardTxt: TextView? = null
    private var missingSensorsSensorsTxt: TextView? = null
    private var directionTxt: TextView? = null
    private var sickTxt: TextView? = null
    private var angleTxt: TextView? = null
    private var temperatureTxt: TextView? = null
    private var weatherTxt: TextView? = null
    private var shakeTxt: TextView? = null
    private var lightTxt: TextView? = null
    private var sleepTxt: TextView? = null
    private var soundTxt: TextView? = null

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_home,
            R.id.navigation_dashboard,
            R.id.navigation_settings,
            R.id.navigation_sensors
        ))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)



        // Permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED)
        {
            val permissions =
                arrayOf(Manifest.permission.RECORD_AUDIO)

            ActivityCompat.requestPermissions(this, permissions,0)
        }
        // this.checkSelfPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")


        // Services
        sensorServiceIntent = Intent(this, SensorsService::class.java)
        activityRecognitionServiceIntent = Intent(this,
            ActivityRecognitionService::class.java)


        // Start Services
        startService(sensorServiceIntent)
        startService(activityRecognitionServiceIntent)

        // Get UI data
        statusCheckBox = binding.root.findViewById(R.id.home_service_status_bool)
        initMissingSensorsTxt()
        angleTxt = binding.root.findViewById(R.id.angleTxt)
        directionTxt = binding.root.findViewById(R.id.directionTxt)
        shakeTxt = binding.root.findViewById(R.id.shakeTxt)
        temperatureTxt = binding.root.findViewById(R.id.temperatureTxt)
        weatherTxt = binding.root.findViewById(R.id.weatherTxt)
        sickTxt = binding.root.findViewById(R.id.sickTxt)
        lightTxt = binding.root.findViewById(R.id.lightTxt)
        sleepTxt = binding.root.findViewById(R.id.sleepTxt)
        soundTxt = binding.root.findViewById(R.id.soundTxt)

        updateNaNValues()

        // Listen to the variables - didn't know how to setup a listener in a right way,
        // so did a loop
        startUIUpdatesCoroutine()
    }

    private fun startUIUpdatesCoroutine() = runBlocking {
        scope.launch {
            delay(1000L)
            while (true) {
                try {
                    statusCheckBox = binding.root.findViewById(R.id.home_service_status_bool)
                    missingSensorsHomeTxt = binding.root.findViewById(R.id.missing_sensors_home)
                    missingSensorsDashboardTxt = binding.root
                        .findViewById(R.id.missing_sensors_dashboard)
                    missingSensorsSensorsTxt = binding.root
                        .findViewById(R.id.missing_sensors_sensors)
                    angleTxt = binding.root.findViewById(R.id.angleTxt)
                    directionTxt = binding.root.findViewById(R.id.directionTxt)
                    shakeTxt = binding.root.findViewById(R.id.shakeTxt)
                    temperatureTxt = binding.root.findViewById(R.id.temperatureTxt)
                    weatherTxt = binding.root.findViewById(R.id.weatherTxt)
                    sickTxt = binding.root.findViewById(R.id.sickTxt)
                    lightTxt = binding.root.findViewById(R.id.lightTxt)
                    sleepTxt = binding.root.findViewById(R.id.sleepTxt)
                    soundTxt = binding.root.findViewById(R.id.soundTxt)

                    delay(10L)
                    if (mSensorBound && mSensorsService.running) {
                        updateCurrentValues()
                    } else {
                        updateNaNValues()
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
        statusCheckBox?.isChecked = true
        /*
        * Missing sensors:
        *  - Ambient Temperature
        *  - Relative Humidity
        *  - Accelerometer
        *  - Magnetometer
        *  - Light
        *  - Audio
        * */
        mSensorsService.resumeReading()
        updateCurrentValues()
    }

    @Suppress("UNUSED_PARAMETER")
    @SuppressLint("SetTextI18n")
    fun pauseReadings(view: View) {
        statusCheckBox?.isChecked = false
        mSensorsService.pauseReading()
        updateMissingSensorsTxt("")
        updateNaNValues()
    }
}

