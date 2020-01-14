package com.motionapps.sensortemplate.model.components

import android.content.Context
import com.motionapps.sensortemplate.model.types.SensorOutput
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

object CSVWriter {
    /**
     * interface for custom csv writer
     */
    interface LineFormatter{
        fun getLine(sensorOutput: SensorOutput): String
    }

    /**
     * get line function takes sensoroutput and formats it to one line of CSV - works with 3D sensors
     */
    class  BasicWriter: LineFormatter{
        override fun getLine(sensorOutput: SensorOutput): String {
            return "%d;%f;%f;%f;%d\n".format(sensorOutput.time, sensorOutput.values[0],
                sensorOutput.values[1], sensorOutput.values[2], sensorOutput.acc)
        }

    }

    /**
     * @param context
     * @param array - array of SensorOutputs to format
     * @param lineFormatter - custom lineformater for csv file
     *
     * creates csv file in internal memory phone, where app is located - accessible only through adb / Android studio
     * path can be changed to external memory - permission required
     * writing is executed on other thread
     */
    fun saveToCSV(context: Context, array: ArrayList<SensorOutput>?, lineFormatter: LineFormatter) {
        val dir = context.filesDir
        Executors.newSingleThreadExecutor().submit {
            val stringBuilder: StringBuilder = StringBuilder()
            if (array != null) {
                for (sensorOutput in array) {
                    stringBuilder.append(lineFormatter.getLine(sensorOutput))
                }
            }

            val file = File(dir, getDate(System.currentTimeMillis()) + ".csv")
            try {
                val writer = FileWriter(file)
                writer.append(stringBuilder.toString())
                writer.flush()
                writer.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * @param time - millis from the beginning - System.currentmillis()
     * @return - string with formatted time
     */

    fun getDate(time: Long): String{
        val simple: DateFormat = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.getDefault())
        val result = Date(time)
        return simple.format(result)
    }




}