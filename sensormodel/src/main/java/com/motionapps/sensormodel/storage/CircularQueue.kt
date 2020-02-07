package com.motionapps.sensormodel.storage

import com.motionapps.sensormodel.types.SensorOutput
import com.motionapps.sensormodel.types.SensorOutputBasic

/**
 * @param c - capacity of storage
 * can be used for temporally storage of samples
 */
class CircularQueue(c: Int) {

    private val capacity: Int = c
    var array: ArrayList<SensorOutput> = ArrayList()

    init {
        // creation of array with empty SensorOutputs
        fillArray()
    }

    /**
     * @param element - SensorOutput is added at the end of array, the oldest element is removed
     */
    fun add(element: SensorOutput) {
            array.removeAt(0)
            array.add(element)
    }

    /**
     * resets array and fills with empty samples
     */
    fun fillArray(){
        array = ArrayList()
        for(i in 0..capacity){
            array.add(SensorOutputBasic())
        }
    }
}

