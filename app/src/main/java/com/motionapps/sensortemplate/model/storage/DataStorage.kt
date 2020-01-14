package com.motionapps.sensortemplate.model.storage

import com.motionapps.sensortemplate.model.types.SensorOutput
import com.motionapps.sensortemplate.model.types.SensorOutputBasic
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap

/**
 * @param sensors - ids of sensors are used as keys for storage
 * @param capacities - define temporal capacities for each sensor
 *
 * every sensor has its own arraylist, which removes the oldes element, if new is added
 * every storage is then stored in ConcurrentHashMap, so multiple threads can access stored samples of different sensors
 */

class DataStorage(sensors: IntArray, capacities: IntArray) {

    private val map: ConcurrentHashMap<Int, CircularQueue> = ConcurrentHashMap()

    init {
        if(sensors.size != capacities.size){
            throw Exception("Sensor count and their capacities must equal")
        }

        for(i in sensors.indices){
            map[sensors[i]] = CircularQueue(capacities[i])
        }
    }

    /**
     * @param sensorOutputBasic - new sample
     * sample is stored to its array
     */
    fun saveSensorOutPut(sensorOutputBasic: SensorOutputBasic){
        map[sensorOutputBasic.sensor]!!.add(sensorOutputBasic)

    }

    /**
     * @param key - key = id of sensor, which is needed
     * new copy of array is created, so recording can proceed even if analysis is taking place
     */
    fun getArrayList(key: Int): ArrayList<SensorOutput>? {
        val arrayList: ArrayList<SensorOutput> = ArrayList()
        arrayList.addAll(map[key]!!.array)
        return arrayList
    }

    /**
     * all arrays are restarted with empty samples
     */
    fun emptyArrays(){
        for((_, queue) in map){
            queue.fillArray()
        }
    }

}