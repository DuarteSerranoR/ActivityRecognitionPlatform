package pie.activityrecognition.platform.android.activityapiservice

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.android.gms.location.*


// The google activity recognition API uses better ML Algorithms for
// better accuracy while measuring the user's activities.

class ActivityRecognitionService : Service() {



    private lateinit var client: ActivityRecognitionClient

    override fun onCreate() {
        super.onCreate()

        client = ActivityRecognition.getClient(this)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, ActivityTransitionReceiver::class.java)
        return PendingIntent.getBroadcast(
            this,
            ActivityTransitionUtils.REQUEST_CODE_INTENT_ACTIVITY_TRANSITION,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
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
        client.requestActivityTransitionUpdates(
            ActivityTransitionUtils.getActivityTransitionRequest(),
            getPendingIntent()
        )
        .addOnSuccessListener {
            //showToast("successful registration")
        }
        .addOnFailureListener { e: Exception ->
            //showToast("Unsuccessful registration")
        }
    }

    private fun removeUpdates() {
        TODO("Not yet implemented")
    }

}
