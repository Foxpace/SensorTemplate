package com.motionapps.sensormodel

import android.content.Context
import android.content.SharedPreferences
import android.hardware.*
import android.location.Location
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.LocationAvailability
import com.motionapps.sensormodel.components.ActivityDetectionManager
import com.motionapps.sensormodel.components.BatteryOptimizer
import com.motionapps.sensormodel.components.SensorHandler
import com.motionapps.sensormodel.components.gps.GPSCallback
import com.motionapps.sensormodel.components.gps.GPSParameters
import com.motionapps.sensormodel.detectors.Detector
import com.motionapps.sensormodel.detectors.DetectorTF


class Model(private val context: Context): GPSCallback.OnLocationChangedCallback, SensorEventListener, TriggerEventListener() {

    private var actualState = BEGIN

    private val sensorHandler: SensorHandler = SensorHandler(context)
    private val activityDetectionManager: ActivityDetectionManager = ActivityDetectionManager(context)
    private val gps : GPSCallback = GPSCallback(context, this@Model, true)
    private val battery : BatteryOptimizer = BatteryOptimizer()
    private val preferences : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private var detector: Detector = DetectorTF(context)

    /**
     * onStart - only at the beginning of the process
     */
    fun onStart(){

        actualState = OPERATING

        if(preferences.getBoolean(WAKE_LOCK, false)) { // checks settings in preferences
            battery.setWakeLockInf(context)
        }

        detector.onStart() // do job before samples arrive
        sensorHandler.registerDetector(detector) // registration of detector

        gps.setGpsParameters(GPSParameters.WALK_PARAMS) // registration of GPS
        gps.changeRequest()

        // registration of proximity sensor for model
        sensorHandler.registerListener(this@Model, Sensor.TYPE_PROXIMITY, SensorManager.SENSOR_DELAY_FASTEST)
        sensorHandler.registerSignificantMotion(this@Model)

        // connection to internet
        Log.i(TAG, "Connection %d".format(BatteryOptimizer.getConnectionType(context)))

    }

    /**
     * resumes if pause was hit - similar to onStart in some ways
     */

    private fun onResume(){

        if (actualState == OPERATING){
            return
        }
        actualState = OPERATING

        if(preferences.getBoolean(WAKE_LOCK, false)) {
            battery.setWakeLockInf(context)
        }

        detector.onResume()
        sensorHandler.resumeSensors()

        gps.setGpsParameters(GPSParameters.WALK_PARAMS)
        gps.changeRequest()
    }

    /**
     * pauses all the sensors and other processes
     */
    private fun onPause(){

        if (actualState == PAUSED){
            return
        }
        actualState = PAUSED
        sensorHandler.pauseSensors()
        detector.onPause()
        gps.gpsOff()

        battery.turnOffWakeLock()
    }

    /**
     * if the model is being destroyed with service
     */
    fun onDestroy(){
        sensorHandler.unregisterListener(this@Model)
        onPause()
        sensorHandler.unregisterAll()
        activityDetectionManager.onDestroy()
        detector.onDestroy()
    }

    /**
     * called when activity recognition sends intent for transition between states
     */
    fun onTransition(result: ActivityTransitionResult){
        Log.i(TAG, "Transition from %s".format(ActivityDetectionManager.convertTransitionToString(result.transitionEvents[0].activityType)))
    }

    /**
     * called when activity recognition sends intent to provide probabilities of movement states
     */
    fun onStateUpdate(result: ActivityRecognitionResult?){
        result?.let {
            Log.i(TAG, "The most probable activity %d".format(ActivityDetectionManager.convertTransitionToString(it.mostProbableActivity.type)))
        }

    }

    /**
     * get type of network, when is changed
     */
    fun onNetworkChange(){
        val type = BatteryOptimizer.getConnectionType(context)
        Log.i(TAG, "Actual network type is %s".format(BatteryOptimizer.getNetworkString(type)))
    }

    fun onBatteryOK() {
        Log.i(TAG, "Battery has been recharged - resuming the model")
        onResume()
    }

    fun onLowBattery() {
        Log.w(TAG, "Battery is low - pausing model")
        onPause()
    }

    override fun onLocationChanged(location: Location?) {
        Log.i(
            TAG, "New location - Lat: %f Lon: %f, Alt: %f, Acc: %f".format(location?.latitude,
            location?.longitude, location?.altitude, location?.accuracy
        ))
    }

    override fun onLastLocationSuccess(location: Location?) {
        Log.i(
            TAG, "Last location - Lat: %f Lon: %f, Alt: %f, Acc: %f".format(location?.latitude,
            location?.longitude, location?.altitude, location?.accuracy
        ))
    }

    override fun onAvailabilityChanged(locationAvailability: LocationAvailability?) {
        if (locationAvailability?.isLocationAvailable!!){
            Log.i(TAG, "Location service is available")
        } else{
            Log.i(TAG, "Location service is not available")
        }
    }


    companion object States{
        // states
        private const val BEGIN = 0
        private const val OPERATING = 1
        private const val PAUSED = 2

        private const val TAG: String = "MainModel"
        private const val WAKE_LOCK: String = "preference_wakelock"
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.i(TAG, "%f proximity".format(event?.values?.get(0)))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onTrigger(event: TriggerEvent?) {
        Log.i(TAG, "Significant motion detected")
        sensorHandler.registerSignificantMotion(this@Model)
    }
}