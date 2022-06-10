
package pie.activityrecognition.platform.android


import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
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
     * */

    /*
     * running
     * (walking)
     * gravity 2x
     *
     * temperature -> too hot, sweats; too cold, trembles and shakes.
     * rainy weather with umbrella -> use humidity and pressure sensors. Research (Geography, meteorology).
     * */

    // TODO - sleeping detection sound and light

    // compare the weather readings, ...., with real meteorology -> gps -> temperature
    // gps, temperature and dry humidity -> beach

    private lateinit var mSensorsService: ActivityRecognitionSensors
    override fun onStart() {
        super.onStart()

        //mViewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        val serviceIntent = Intent(this, ActivityRecognitionSensors::class.java)
        // Start Service
        startService(serviceIntent)
        // Bind to Service
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE)
    }

    var mBound = false
    private val mConnection = object : ServiceConnection {
        // Called when the connection with the service is established
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // Because we have bound to an explicit
            // service that is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            val binder = service as ActivityRecognitionSensors.LocalBinder
            mSensorsService = binder.getService()
            mBound = true
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {

            /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS),0)
            }*/

            //if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            //if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            //ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),1)
            //        ActivityCompat.requestPermissions(this, arrayOf("com.google.android.gms.permission.ACTIVITY_RECOGNITION"),1)
            //}



            // Sensors:
            // start your next activity
            //startActivity(sensorsAPI)

            //sensorsAPI = Indent(this, ActivityRecognitionSensors::class.java)
            //startActivity(sensorsAPI)
            //sensorsAPI = fm.findFragmentByTag("ActivityRecognitionSensors") as ActivityRecognitionSensors




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
        } catch (e: Exception) {
            println(e)
        }
    }

    fun resumeReadings(view: View) {
        mSensorsService.resumeReading()
    }

    fun pauseReadings(view: View) {
        mSensorsService.pauseReading()
    }

}


