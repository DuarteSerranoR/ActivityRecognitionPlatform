package pie.activity_recognition.activityapi_service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import java.util.concurrent.TimeUnit

class ActivityTransitionReceiver : BroadcastReceiver() {

    var running: Boolean = false
    var runningTime: Long = 0 // in seconds
    var runningPercentage: Float = 0f

    var walking: Boolean = false
    var walkingTime: Long = 0 // in seconds
    var walkingPercentage: Float = 0f

    private val initTime: Long = TimeUnit.SECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS) // in seconds since a certain point in time

    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            result?.let {
                result.transitionEvents.forEach { event ->

                    // Running
                    if (event.activityType == DetectedActivity.RUNNING) {
                        when (event.transitionType) {
                            // Started running
                            ActivityTransition.ACTIVITY_TRANSITION_ENTER -> {
                                running = true
                            }
                            // Stopped running
                            ActivityTransition.ACTIVITY_TRANSITION_EXIT -> {
                                running = false
                                runningTime += TimeUnit.SECONDS.convert(event.elapsedRealTimeNanos, TimeUnit.NANOSECONDS)
                            }
                            else -> {
                                println("Something went wrong on detecting the transition type.")
                            }
                        }
                    }

                    // Walking
                    else if (event.activityType == DetectedActivity.WALKING) {
                        when (event.transitionType) {
                            // Started walking
                            ActivityTransition.ACTIVITY_TRANSITION_ENTER -> {
                                walking = true
                            }
                            // Stopped walking
                            ActivityTransition.ACTIVITY_TRANSITION_EXIT -> {
                                walking = false
                                walkingTime += TimeUnit.SECONDS.convert(event.elapsedRealTimeNanos, TimeUnit.NANOSECONDS)
                            }
                            else -> {
                                println("Something went wrong on detecting the transition type.")
                            }
                        }
                    }

                    val currentTimeSecs = TimeUnit.SECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS)
                    val currentTotalElapsedTime: Long = currentTimeSecs - initTime

                    // Running
                    runningPercentage = ((runningTime / currentTotalElapsedTime) * 100).toFloat()
                    // TODO - use the UI to always keep it current, get the current status,
                    //  time between the last update and current, and make the percentage.

                    // Walking
                    walkingPercentage = ((walkingTime / currentTotalElapsedTime) * 100).toFloat()


                }
            }
        }
    }
}