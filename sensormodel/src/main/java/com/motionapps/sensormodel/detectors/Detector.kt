package com.motionapps.sensormodel.detectors

import android.hardware.SensorEventListener
import com.motionapps.sensormodel.storage.DataStorage

abstract class Detector: SensorEventListener{

    abstract val sensors: IntArray // needed sensors - id
    abstract val sampling: IntArray // sampling of each sensor
    abstract val timeFrame: IntArray // needed timeframe for sensor
    abstract val storage: DataStorage // temporally storage for samples
    val TAG: String = this.javaClass.name

    // methods are called by model
    abstract fun onStart()
    abstract fun onResume()
    abstract fun onPause()
    abstract fun onDestroy()

}