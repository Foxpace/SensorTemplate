package com.motionapps.sensormodel.types

/**
 * basic class for sample from sensor
 */
abstract class SensorOutput {
    abstract val values: FloatArray
    abstract val acc: Int
    abstract val time: Long
    abstract val sensor: Int
}