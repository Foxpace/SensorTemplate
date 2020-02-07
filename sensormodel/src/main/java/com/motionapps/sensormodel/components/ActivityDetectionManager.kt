package com.motionapps.sensormodel.components

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener

class ActivityDetectionManager(context: Context) {

    private val activityRecognitionClient: ActivityRecognitionClient = ActivityRecognition.getClient(context)

    private var pendingIntentTransition: PendingIntent? = null
    private var pendingIntentUpdates: PendingIntent? = null

    /**
     * @param activities - ints of activities from ActivityRecognitionClient
     * creates arraylist of activity transitions
     */
    private fun getTransitions(activities: IntArray): ArrayList<ActivityTransition>{
        val transitions: ArrayList<ActivityTransition> = ArrayList()
        for (activity in activities){

            transitions.add(
                ActivityTransition.Builder()
                    .setActivityType(activity)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build()
            )
            transitions.add(
                ActivityTransition.Builder()
                    .setActivityType(activity)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    .build()
            )
        }

        return transitions
    }

    /**
     * creates pending intent, which are passed to model, when they are triggered
     */
    private fun getPendingIntentUpdates(context: Context): PendingIntent {
        val intent = Intent(MOVING_STATE_UPDATES)
        return PendingIntent.getBroadcast(
            context, REQUEST_CODE_UPDATES,
            intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getPendingIntentTransition(context: Context): PendingIntent {
        val intentTransitions = Intent(MOVING_STATE_TRANSITION)
        return PendingIntent.getBroadcast(
            context, REQUEST_CODE_TRANSITION,
            intentTransitions, PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * @param context
     * @param activities - wanted activities in int arraylist
     * @param positiveCallBack - custom callback
     * @param negativeCallBack - custom callback
     *
     * when transition between 2 states occurs (from walking to driving) - intent with entered and exited state is sent
     */
    fun registerTransitions(
        context: Context,
        activities: IntArray,
        positiveCallBack: OnSuccessListener<Void>,
        negativeCallBack: OnFailureListener)
    {
        pendingIntentTransition = getPendingIntentTransition(context)

        activityRecognitionClient.requestActivityTransitionUpdates(
            ActivityTransitionRequest(getTransitions(activities)),
            pendingIntentTransition
        ).addOnSuccessListener(positiveCallBack)
            .addOnFailureListener(negativeCallBack)
    }

    /**
     * @param context
     * @param activities - wanted activities in int arraylist
     * overloaded method, without custom callbacks
     */

    fun registerTransitions(context: Context, activities: IntArray) {

        pendingIntentTransition = getPendingIntentTransition(context)

        activityRecognitionClient.requestActivityTransitionUpdates(ActivityTransitionRequest(
            getTransitions(activities)),
            pendingIntentTransition
        ).addOnSuccessListener {
            OnSuccessListener<Void> {
                Log.i(TAG, "successfully registered activity transitions")
            }
        }.addOnFailureListener {
            pendingIntentTransition = null
            Log.w(TAG, "Registration of activity transitions failed")
        }
    }

    /**
     * @param context
     * @param timeToUpdate - time, in which intent will be delivered - it is not guaranteed by system
     * @param positiveCallBack - custom callback
     * @param negativeCallBack - custom callback
     *
     * intent with probabilities of every category of movement is sent
     */
    fun registerUpdates(
        context: Context,
        timeToUpdate: Long,
        positiveCallBack: OnSuccessListener<Void>,
        negativeCallBack: OnFailureListener)
    {
        pendingIntentUpdates = getPendingIntentUpdates(context)

        activityRecognitionClient.requestActivityUpdates(timeToUpdate, pendingIntentUpdates).
            addOnSuccessListener(positiveCallBack)
            .addOnFailureListener(negativeCallBack)
    }

    /**
     * @param context
     * @param timeToUpdate - time, in which intent will be delivered - it is not guaranteed by system
     *
     * intent with probabilities of every category of movement is sent
     */

    fun registerUpdates(context: Context, timeToUpdate: Long) {

        pendingIntentUpdates = getPendingIntentUpdates(context)

        activityRecognitionClient.requestActivityUpdates(timeToUpdate, pendingIntentUpdates).
            addOnSuccessListener {
            OnSuccessListener<Void> {
                Log.i(TAG, "successfully registered activity updates")
            }
        }.addOnFailureListener {
            pendingIntentUpdates = null
            Log.w(TAG, "Registration of activity updates failed")
        }
    }

    /**
     * removes all updates from Activity recognition client
     */
    fun onDestroy() {
        if (pendingIntentTransition != null) {
            activityRecognitionClient.removeActivityTransitionUpdates(pendingIntentTransition)
            pendingIntentTransition = null
        }

        if (pendingIntentUpdates != null) {
            activityRecognitionClient.removeActivityUpdates(pendingIntentUpdates)
            pendingIntentUpdates = null
        }

        Log.i(TAG, "unregistering")
    }

    companion object{


        const val MOVING_STATE_TRANSITION: String = "MOVING_STATE_TRANSITION"
        const val MOVING_STATE_UPDATES: String = "MOVING_STATE_UPDATES"

        private val INTENTS: Array<String> = arrayOf(MOVING_STATE_TRANSITION, MOVING_STATE_UPDATES)

        fun registerIntents(intentFilter: IntentFilter){
            for(s in INTENTS){
                intentFilter.addAction(s)
            }
        }

        //  more options
        val MOVING_PERSON_ACTIVITIES: IntArray = intArrayOf(
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.STILL,
            DetectedActivity.ON_FOOT,
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING
        )

        fun convertTransitionToString(transition: Int): String {
            return when (transition) {
                DetectedActivity.IN_VEHICLE -> "Vehicle"
                DetectedActivity.ON_BICYCLE -> "Bike"
                DetectedActivity.ON_FOOT -> "Foot"
                DetectedActivity.STILL -> "Still"
                DetectedActivity.WALKING -> "Walking"
                DetectedActivity.RUNNING -> "Run"
                else -> "Unknown"
            }
        }

        private const val TAG: String = "ActivityManager"
        private const val REQUEST_CODE_TRANSITION = 1654
        private const val REQUEST_CODE_UPDATES = 457
    }
}