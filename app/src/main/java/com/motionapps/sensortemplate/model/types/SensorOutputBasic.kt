package com.motionapps.sensortemplate.model.types

import android.hardware.SensorEvent

/**
 * can create empty samples / be filled with SensorEvent object - values are copied to prevent references to other objects
 */
open class SensorOutputBasic(array: FloatArray, accuracy: Int, timeStamp: Long, type: Int): SensorOutput() {

    constructor(): this(floatArrayOf(0f,0f,0f), 0, 0, 0)
    constructor(s: SensorEvent): this(s.values.copyOf(), s.accuracy, s.timestamp, s.sensor.type)

    override val values: FloatArray = array
    override val acc: Int = accuracy
    override val time: Long = timeStamp
    override val sensor: Int = type

}