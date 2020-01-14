package com.motionapps.sensortemplate.model.detectors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.util.Log
import com.motionapps.sensortemplate.model.components.CSVWriter
import com.motionapps.sensortemplate.model.components.SensorHandler
import com.motionapps.sensortemplate.model.storage.DataStorage
import com.motionapps.sensortemplate.model.types.SensorOutputBasic
import com.motionapps.sensortemplate.model.types.SensorOutputExtended

/**
 * basic sensor example
 */
class DetectorKotlin(private val context: Context) : Detector() {

    private val time: Int = 10000 // ms - custom window frame

    override val sensors: IntArray = intArrayOf(Sensor.TYPE_ACCELEROMETER)
    override val sampling: IntArray = intArrayOf(SensorManager.SENSOR_DELAY_FASTEST)
    override val timeFrame: IntArray = intArrayOf(time)
    override val storage: DataStorage = DataStorage(sensors, SensorHandler.getCapacity(context, sensors, timeFrame))


    override fun onStart(){

    }

    override fun onResume() {

    }

    override fun onPause() {
        CSVWriter.saveToCSV(context, storage.getArrayList(Sensor.TYPE_ACCELEROMETER), CSVWriter.BasicWriter())
        storage.emptyArrays()
    }

    override fun onDestroy() {

    }

    override fun onSensorChanged(s: SensorEvent) {
        when(s.sensor.type){

            Sensor.TYPE_ACCELEROMETER -> {
                val acceleration = SensorOutputExtended(s) // stored in SensorOutput object
                storage.saveSensorOutPut(acceleration) // temporally stored
                checkAcceleration(acceleration) // your detection
            }
            else -> {
                storage.saveSensorOutPut(SensorOutputBasic(s)) // in other case just store
            }
        }
    }

    override fun onAccuracyChanged(s: Sensor, a: Int) {}

    private fun checkAcceleration(acceleration: SensorOutputExtended){
        Log.i(TAG, "Acceleration: %f m/s2".format(acceleration.magnitude))
        // add your conditions / algorithm
    }


}