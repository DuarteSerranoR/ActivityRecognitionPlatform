package pie.activity_recognition.activityapi_service

import android.Manifest
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient

// The google activity recognition API uses better ML Algorithms for
// better accuracy while measuring the user's activities.

class ActivityRecognitionService : Service() {



    private lateinit var client: ActivityRecognitionClient

    override fun onCreate() {
        super.onCreate()

        client = ActivityRecognition.getClient(this)
        requestForUpdates()

    }

    override fun onDestroy() {
        removeUpdates()
        deregisterForUpdates()
        super.onDestroy()
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, ActivityTransitionReceiver::class.java)
        return PendingIntent.getBroadcast(
            this,
            ActivityTransitionUtils.REQUEST_CODE_INTENT_ACTIVITY_TRANSITION,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }




    // Service binder

    private val mBinder : LocalBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): ActivityRecognitionService = this@ActivityRecognitionService
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        //stopSelf() -> if we want it to not run on background when the app is closed
        super.onTaskRemoved(rootIntent)
    }




    // Google's Activity Recognition Functions

    private fun requestForUpdates() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                println("Permission not granted for the use of the google's ActivityRecognitionAPI!")
                return
            }
        client.requestActivityTransitionUpdates(
            ActivityTransitionUtils.getActivityTransitionRequest(),
            getPendingIntent()
        )
            .addOnSuccessListener {
                println("Successful registration")
            }
            .addOnFailureListener { e: Exception ->
                println("Unsuccessful registration with exception: " + e.message)
            }
    }

    private fun deregisterForUpdates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                println("Permission not granted for the use of the google's ActivityRecognitionAPI!")
                return
            }

        client
            .removeActivityTransitionUpdates(getPendingIntent())
            .addOnSuccessListener {
                getPendingIntent().cancel()
                println("successful deregistration")
            }
            .addOnFailureListener { e: Exception ->
                println("unsuccessful deregistration")
            }
    }

    private fun removeUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED
        ) {
            println("No Activity Recognition permissions!")
            return
        }
        client.removeActivityUpdates(getPendingIntent())
    }

}
