package com.motionapps.sensortemplate.model.types

import android.hardware.SensorEvent
import kotlin.math.sqrt

class SensorOutputExtended(s: SensorEvent): SensorOutputBasic(s) {

    /**
     * magnitude of values is calculated for every sample
     */
    val magnitude: Double
    init {
        var m = 0.0
        for (f: Float in this.values){
            m += f*f
        }
        magnitude = sqrt(m)

    }
}