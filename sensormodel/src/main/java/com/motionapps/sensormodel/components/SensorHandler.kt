package com.motionapps.sensormodel.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.TriggerEventListener
import android.util.Log
import com.motionapps.sensormodel.detectors.Detector
import kotlin.Exception

class SensorHandler(service: Context) {

    private val sensorManager: SensorManager = service.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var sensorEventListeners: ArrayList<SensorEventListener> = ArrayList()
    private var sensorListenersIds: ArrayList<SensorInfo> = ArrayList()
    private var counterListeners: Int = 0
    private var paused : Boolean = false

    /**
     * @param detector - has incorporated sensor arrays in itself
     * registers sensors for detector
     */
    fun registerDetector(detector: Detector){

        if (detector.sensors.size != detector.sampling.size) {
            throw Exception("Number of sensors must be equal to sampling speeds")
        }

        for (i: Int in detector.sensors.indices){
            val sensor = sensorManager.getDefaultSensor(detector.sensors[i])

            if (sensorManager.registerListener(detector, sensor, detector.sampling[i])){
                sensorEventListeners.add(detector)
                Log.i(TAG, String.format("Detector %s registered", sensor.name))
            }else{
                Log.e(TAG, String.format("Detector not available %s", sensor.name))
            }
        }
    }

    /**
     * @param listener - other SensorEventListener then Detector
     * @param sensorType - integer type of sensor to register
     * @param sampling - sampling from SensorManager
     */
    fun registerListener(listener: SensorEventListener, sensorType: Int, sampling: Int): Boolean{

        val sensor = sensorManager.getDefaultSensor(sensorType)

        return if (sensorManager.registerListener(listener, sensor, sampling)){
            sensorEventListeners.add(listener)
            sensorListenersIds.add(SensorInfo(listener.hashCode(), sampling, sensorType))
            Log.i(TAG, String.format("Detector %s registered", sensor.name))
            true
        }else{
            Log.e(TAG, String.format("Detector not available %", sensor.name))
            false
        }
    }

    /**
     * pauses all the registered sensors
     */
    fun pauseSensors(){
        if(paused){
            return
        }

        for (eventListener: SensorEventListener in sensorEventListeners){
            sensorManager.unregisterListener(eventListener)
        }
        paused = true
    }

    /**
     * resumes all sensors
     */

    fun resumeSensors(){
        for (eventListener: SensorEventListener in sensorEventListeners){
            if(eventListener is Detector){
                for (i: Int in eventListener.sensors.indices){
                    val sensor = sensorManager.getDefaultSensor(eventListener.sensors[i])

                    if (!sensorManager.registerListener(eventListener, sensor, eventListener.sampling[i])){
                        Log.e(TAG, String.format("Detector not available %", sensor.name))
                    }
                }
            }else{
                val sensor = sensorManager.getDefaultSensor(sensorListenersIds[counterListeners].sensor)
                if (!sensorManager.registerListener(eventListener, sensor, sensorListenersIds[counterListeners].sampling)){
                    Log.e(TAG, String.format("Detector not available %", sensor.name))
                }
                counterListeners++
            }
        }
        counterListeners = 0
        paused = false

    }

    /**
     * @param eventListener - SensorEventListener
     * unregisters one listener
     */

    fun unregisterListener(eventListener: SensorEventListener){

        sensorManager.unregisterListener(eventListener)
        sensorEventListeners.remove(eventListener)

        if (eventListener !is Detector){

            val l = sensorListenersIds.size

            for (sensorInfo: SensorInfo in sensorListenersIds){
                if(eventListener.hashCode() == sensorInfo.hash){
                    sensorListenersIds.remove(sensorInfo)
                    Log.i(TAG, "Sensor unregistered %s".format(sensorManager.getDefaultSensor(sensorInfo.sensor).name))
                    break
                }
            }

            if(l - 1 != sensorListenersIds.size){
                throw java.lang.Exception("Unknown event listener is being removed")
            }

        }else{
            Log.i(TAG, "Sensor unregistered %s".format(eventListener.TAG))
        }
    }

    /**
     * unregisters all sensors
     */
    fun unregisterAll(){
        pauseSensors()
        sensorEventListeners = ArrayList()
        sensorListenersIds = ArrayList()
    }

    /**
     * @param listener - TriggerEventListener - for Significant Motion sensor - when is triggered it is unregistered too
     * @return boolean - success of registration
     */
    fun registerSignificantMotion(listener: TriggerEventListener): Boolean {
        val mSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)
        mSensor?.also { sensor ->
            Log.i(TAG, "Significant sensor registered")
            sensorManager.requestTriggerSensor(listener, sensor)
            return true
        }
        Log.e(TAG, "Significant sensor missing")
        return false
    }

    /**
     * helper class to store info about other sensor then detectors
     */
    inner class SensorInfo(val hash: Int, val sampling: Int, val sensor: Int)

    companion object{

        /**
         * @param context
         * @param sensors - int array of ids of sensors
         * @param timeFrames - length of time frame for sensors to store in ms
         * @return - for example: 10000 ms frame for sensor with minimal delay of 10 ms results in frame with 1000 samples
         */
        fun getCapacity(context: Context, sensors: IntArray, timeFrames: IntArray): IntArray {
            val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val samples = IntArray(sensors.size)

            if(sensors.size != timeFrames.size){
                throw Exception("Number of requested sensors must equal with ")
            }

            for(i in sensors.indices){

                val sensor: Sensor = sensorManager.getDefaultSensor(sensors[i])
                val minDelay: Int = sensor.minDelay/1000

                if (minDelay == 0){
                    throw Exception("Sensor %s is on change sensor".format(sensor.name))
                }

                samples[i] = timeFrames[i]/minDelay
            }

            return samples
        }

        private const val TAG: String = "SensorHandler"
    }


}