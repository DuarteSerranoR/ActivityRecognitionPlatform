package pie.activityrecognition.platform.android.activityapiservice

import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity

object ActivityTransitionUtils {

    const val REQUEST_CODE_ACTIVITY_TRANSITION = 123
    const val REQUEST_CODE_INTENT_ACTIVITY_TRANSITION = 122
    const val ACTIVITY_TRANSITION_NOTIFICATION_ID = 111
    const val ACTIVITY_TRANSITION_STORAGE = "ACTIVITY_TRANSITION_STORAGE"

    /*fun hasActivityTransitionPermissions(context: Context): Boolean =
        Eas*/


    private fun getTransitions() : MutableList<ActivityTransition> {

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


        return transitions
    }

    fun getActivityTransitionRequest() = ActivityTransitionRequest(getTransitions())
/*
    @RequiresApi(Build.VERSION_CODES.Q)
    fun hasActivityTransitionPermissions(context: Context): Boolean =
        EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
*/
    fun toActivityString(activity: Int): String {
        return when (activity) {
            DetectedActivity.STILL -> "STILL"
            DetectedActivity.WALKING -> "WALKING"
            DetectedActivity.IN_VEHICLE -> "IN VEHICLE"
            DetectedActivity.RUNNING -> "RUNNING"
            else -> "UNKNOWN"
        }
    }

    fun toTransitionType(transitionType: Int): String  {
        return when (transitionType) {
            ActivityTransition.ACTIVITY_TRANSITION_ENTER -> "ENTER"
            ActivityTransition.ACTIVITY_TRANSITION_EXIT -> "EXIT"
            else -> "UNKNOWN"
        }
    }
}