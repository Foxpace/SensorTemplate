package com.motionapps.sensortemplate.model

import android.content.SharedPreferences
import android.hardware.*
import android.location.Location
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.LocationAvailability
import com.motionapps.sensortemplate.activities.components.Notify
import com.motionapps.sensortemplate.activities.Options
import com.motionapps.sensortemplate.model.components.ActivityDetectionManager
import com.motionapps.sensortemplate.model.components.BatteryOptimizer
import com.motionapps.sensortemplate.model.components.gps.GPSCallback
import com.motionapps.sensortemplate.model.components.gps.GPSCallback.OnLocationChangedCallback
import com.motionapps.sensortemplate.model.components.gps.GPSParameters
import com.motionapps.sensortemplate.model.components.SensorHandler
import com.motionapps.sensortemplate.model.detectors.Detector
import com.motionapps.sensortemplate.model.detectors.DetectorTF
import com.motionapps.sensortemplate.service.DetectionService

class Model(private val detectionService: DetectionService): OnLocationChangedCallback, SensorEventListener, TriggerEventListener() {

    private val TAG: String = "MainModel"

    private var actualState = BEGIN

    private val sensorHandler: SensorHandler = SensorHandler(detectionService)
    private val activityDetectionManager: ActivityDetectionManager = ActivityDetectionManager(detectionService)
    private val gps : GPSCallback = GPSCallback(detectionService, this@Model, true)
    private val battery : BatteryOptimizer = BatteryOptimizer()
    private val preferences : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(detectionService)
    private var detector: Detector = DetectorTF(detectionService)

    /**
     * onStart - only at the beginning of the process
     */
    fun onStart(){

        actualState = OPERATING

        if(preferences.getBoolean(Options.WAKE_LOCK, false)) { // checks settings in preferences
            battery.setWakeLockInf(detectionService)
        }

        detector.onStart() // do job before samples arrive
        sensorHandler.registerDetector(detector) // registration of detector

        gps.setGpsParameters(GPSParameters.WALK_PARAMS) // registration of GPS
        gps.changeRequest()

        // registration of proximity sensor for model
        sensorHandler.registerListener(this@Model, Sensor.TYPE_PROXIMITY, SensorManager.SENSOR_DELAY_FASTEST)
        sensorHandler.registerSignificantMotion(this@Model)

        // connection to internet
        Log.i(TAG, "Connection %d".format(battery.getConnectionType(detectionService)))

    }

    /**
     * resumes if pause was hit - similar to onStart in some ways
     */

    private fun onResume(){

        if (actualState == OPERATING){
            return
        }
        actualState = OPERATING

        if(preferences.getBoolean(Options.WAKE_LOCK, false)) {
            battery.setWakeLockInf(detectionService)
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
    fun onStateUpdate(result: ActivityRecognitionResult){
        Log.i(TAG, "The most probable activity %d".format(ActivityDetectionManager.convertTransitionToString(result.mostProbableActivity.type)))
    }

    /**
     * get type of network, when is changed
     */
    fun onNetworkChange(){
        val type = battery.getConnectionType(detectionService)
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
        Log.i(TAG, "New location - Lat: %f Lon: %f, Alt: %f, Acc: %f".format(location?.latitude,
            location?.longitude, location?.altitude, location?.accuracy
        ))
    }

    override fun onLastLocationSuccess(location: Location?) {
        Log.i(TAG, "Last location - Lat: %f Lon: %f, Alt: %f, Acc: %f".format(location?.latitude,
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
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.i(TAG, "%f proximity".format(event?.values?.get(0)))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onTrigger(event: TriggerEvent?) {
        Log.i(TAG, "Significant motion detected")
        sensorHandler.registerSignificantMotion(this@Model)
        Notify.vibrate(detectionService)
    }
}