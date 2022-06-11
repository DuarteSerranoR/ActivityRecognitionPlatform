
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

    private lateinit var serviceIntent: Intent
    private lateinit var mSensorsService: SensorsService
    private val scope = MainScope()
    override fun onStart() {
        super.onStart()
        // Bind to Service
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE)
    }

    var mBound = false
    private val mConnection = object : ServiceConnection {
        // Called when the connection with the service is established
        @SuppressLint("SetTextI18n")
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // Because we have bound to an explicit
            // service that is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            val binder = service as SensorsService.LocalBinder
            mSensorsService = binder.getService()
            mBound = true
            if (mSensorsService.running) {
                angleTxt.text = "Angle: " + mSensorsService.angle + "º"
                directionTxt.text = "Direction: " + mSensorsService.direction
                shakeTxt.text = "Shake: " + mSensorsService.shake
                temperatureTxt.text = "Temperature: " + mSensorsService.temperatureStatus
                weatherTxt.text = "Weather: " + mSensorsService.weather
                sickTxt.text = "Sick: " + mSensorsService.sick
            } else {
                angleTxt.text = "Angle: NaN"
                directionTxt.text = "Direction: NaN"
                shakeTxt.text = "Shake: NaN"
                temperatureTxt.text = "Temperature: NaN"
                weatherTxt.text = "Weather: NaN"
                sickTxt.text = "Sick: NaN"
            }
        }

        // Called when the connection with the service disconnects unexpectedly
        override fun onServiceDisconnected(className: ComponentName) {
            //Log.e(TAG, "onServiceDisconnected")
            mBound = false
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(mConnection)
    }


    private lateinit var directionTxt: TextView
    private lateinit var sickTxt: TextView
    private lateinit var angleTxt: TextView
    private lateinit var temperatureTxt: TextView
    private lateinit var weatherTxt: TextView
    private lateinit var shakeTxt: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        serviceIntent = Intent(this, SensorsService::class.java)
        // Start Service
        startService(serviceIntent)

        // Get UI
        angleTxt = findViewById(R.id.angleTxt)
        directionTxt = findViewById(R.id.directionTxt)
        shakeTxt = findViewById(R.id.shakeTxt)
        temperatureTxt = findViewById(R.id.temperatureTxt)
        weatherTxt = findViewById(R.id.weatherTxt)
        sickTxt = findViewById(R.id.sickTxt)

        angleTxt.text = "Angle: NaN"
        directionTxt.text = "Direction: NaN"
        shakeTxt.text = "Shake: NaN"
        temperatureTxt.text = "Temperature: NaN"
        weatherTxt.text = "Weather: NaN"
        sickTxt.text = "Sick: NaN"

        try {

            // Google's ActivityRecognition API:
            /*
            val transitions = mutableListOf<ActivityTransition>()

            // Running
            transitions +=
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.RUNNING)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build()
            transitions +=
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.RUNNING)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    .build()

            // Walking
            transitions +=
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.WALKING)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build()
            transitions +=
                ActivityTransition.Builder()
                    .setActivityType(DetectedActivity.WALKING)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    .build()


            val request = ActivityTransitionRequest(transitions)

            // The execution

            ////------
            val updatedIntent = Intent(applicationContext, MainActivity::class.java)
            /*.apply {
            action = NOTIFICATION_ACTION
            data = differentDeepLink
        }*/
            // Because we're passing `FLAG_UPDATE_CURRENT`, this updates
            // the existing PendingIntent with the changes we made above.
            val NOTIFICATION_REQUEST_CODE = 200;
            val googleApiPendingIntent = PendingIntent.getActivity(
                applicationContext,
                NOTIFICATION_REQUEST_CODE,
                updatedIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            // The PendingIntent has been updated.
            ////------

            // myPendingIntent is the instance of PendingIntent where the app receives callbacks.
            val task = ActivityRecognitionClient(this).requestActivityTransitionUpdates(request,
                TransitionsReceiver.getPendingIntent(this))
            //ActivityRecognition.getClient(this) // this = context
            //    .requestActivityTransitionUpdates(request, googleApiPendingIntent)

            task.addOnSuccessListener {
                // Handle success
            }

            task.addOnFailureListener { e: Exception ->
                // Handle error
            }

            // ON TERMINATION OF THE APP!!
            // myPendingIntent is the instance of PendingIntent where the app receives callbacks.
            val taskRemoval = ActivityRecognitionAPI.getClient(this)
                .removeActivityTransitionUpdates(googleApiPendingIntent)

            taskRemoval.addOnSuccessListener {
                googleApiPendingIntent.cancel()
            }

            taskRemoval.addOnFailureListener { e: Exception ->
                //Log.e("MYCOMPONENT", e.message)
                Log.e("MYCOMPONENT", "error")
            }
            */

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
                    if (mBound && mSensorsService.running) {
                        angleTxt.text = "Angle: " + mSensorsService.angle + "º"
                        directionTxt.text = "Direction: " + mSensorsService.direction
                        shakeTxt.text = "Shake: " + mSensorsService.shake
                        temperatureTxt.text = "Temperature: " + mSensorsService.temperatureStatus
                        weatherTxt.text = "Weather: " + mSensorsService.weather
                        sickTxt.text = "Sick: " + mSensorsService.sick
                    }
                } catch (e: Exception) {
                    println(e)
                }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @SuppressLint("SetTextI18n")
    fun resumeReadings(view: View) {
        mSensorsService.resumeReading()
        angleTxt.text = "Angle: " + mSensorsService.angle + "º"
        directionTxt.text = "Direction: " + mSensorsService.direction
        shakeTxt.text = "Shake: " + mSensorsService.shake
        temperatureTxt.text = "Temperature: " + mSensorsService.temperatureStatus
        weatherTxt.text = "Weather: " + mSensorsService.weather
        sickTxt.text = "Sick: " + mSensorsService.sick
    }

    @Suppress("UNUSED_PARAMETER")
    @SuppressLint("SetTextI18n")
    fun pauseReadings(view: View) {
        mSensorsService.pauseReading()
        angleTxt.text = "Angle: NaN"
        directionTxt.text = "Direction: NaN"
        shakeTxt.text = "Shake: NaN"
        temperatureTxt.text = "Temperature: NaN"
        weatherTxt.text = "Weather: NaN"
        sickTxt.text = "Sick: NaN"
    }

}


