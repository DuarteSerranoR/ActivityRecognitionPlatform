package pie.activityrecognition.platform.android.activityapiservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityTransitionResult

class ActivityTransitionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            result?.let {
                result.transitionEvents.forEach { event ->
                    //Info for debugging purposes
                    /*
                    val info =
                        "Transition: " + ActivityTransitionsUtil.toActivityString(event.activityType) +
                                " (" + ActivityTransitionsUtil.toTransitionType(event.transitionType) + ")" + "   " +
                                SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())


                    Notify
                        .with(context)
                        .content {
                            title = "Activity Detected"
                            text =
                                "I can see you are in ${
                                    ActivityTransitionsUtil.toActivityString(
                                        event.activityType
                                    )
                                } state"
                        }
                        .show(id = Constants.ACTIVITY_TRANSITION_NOTIFICATION_ID)

                    Toast.makeText(context, info, Toast.LENGTH_LONG).show()
                    */
                }
            }
        }
    }
}