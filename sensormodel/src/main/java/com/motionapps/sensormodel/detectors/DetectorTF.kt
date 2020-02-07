package com.motionapps.sensormodel.detectors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.widget.Toast
import com.motionapps.classifiers.tensorflow.TensorflowLiteClassifier
import com.motionapps.sensormodel.components.SensorHandler
import com.motionapps.sensormodel.storage.DataStorage
import com.motionapps.sensormodel.types.SensorOutputBasic
import com.motionapps.sensormodel.types.SensorOutputExtended

/**
 *  example based on Tensorflow Lite neural network - template
 */
class DetectorTF(private val context: Context) : Detector() {

    private val time: Int = 10000 // ms

    private var analysing: Boolean = false
    private var startTime: Long = 0L

    // init of tensorflow neural network
    private val tensorflowLiteClassifier: TensorflowLiteClassifier = TensorflowLiteClassifier.create(context)!!

    override val sensors: IntArray = intArrayOf(Sensor.TYPE_ACCELEROMETER)
    override val sampling: IntArray = intArrayOf(SensorManager.SENSOR_DELAY_FASTEST)
    override val timeFrame: IntArray = intArrayOf(time)
    override val storage: DataStorage = DataStorage(sensors,
        SensorHandler.getCapacity(context, sensors, timeFrame))


    override fun onStart(){

    }

    override fun onResume() {

    }

    override fun onPause() {
        storage.emptyArrays()
    }

    override fun onDestroy() {
        TensorflowLiteClassifier.destroy()
    }

    override fun onSensorChanged(s: SensorEvent) {
        when(s.sensor.type){

            Sensor.TYPE_ACCELEROMETER -> {
                val acceleration = SensorOutputExtended(s)
                storage.saveSensorOutPut(acceleration)
                checkAcceleration(acceleration)
            }
            else -> {
                storage.saveSensorOutPut(SensorOutputBasic(s))
            }
        }
    }

    override fun onAccuracyChanged(s: Sensor, a: Int) {}

    private fun checkAcceleration(acceleration: SensorOutputExtended){

        // example of usage
        if(acceleration.magnitude > 30 && !analysing){
            analysing = true
            startTime = System.currentTimeMillis()

            val result = tensorflowLiteClassifier.classifyEvent(
                arrayListOf(0.0, 0.0, 0.0, 0.0, 0.0))

            Toast.makeText(context, result.toString(), Toast.LENGTH_SHORT).show()

        }else if(analysing && System.currentTimeMillis() - startTime > 5000L){
            analysing = false
        }
    }


}