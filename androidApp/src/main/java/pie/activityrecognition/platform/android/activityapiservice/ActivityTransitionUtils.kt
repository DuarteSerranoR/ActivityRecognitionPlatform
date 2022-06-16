package pie.activityrecognition.platform.android.activityapiservice

import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity

object ActivityTransitionUtils {

    const val REQUEST_CODE_ACTIVITY_TRANSITION = 123
    const val REQUEST_CODE_INTENT_ACTIVITY_TRANSITION = 122
    const val ACTIVITY_TRANSITION_NOTIFICATION_ID = 111
    const val ACTIVITY_TRANSITION_STORAGE = "ACTIVITY_TRANSITION_STORAGE"


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

}