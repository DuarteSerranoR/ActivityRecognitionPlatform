package pie.activityrecognition.platform.android

import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import java.lang.Exception


class SensorService : JobIntentService() {

    override fun onHandleWork(workIntent: Intent) {

        //val dataString = workIntent.dataString

        try {
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
        } catch (e: Exception) { // TODO - replace with an exception handler for this types of services
            println(e)
        }
    }
}
