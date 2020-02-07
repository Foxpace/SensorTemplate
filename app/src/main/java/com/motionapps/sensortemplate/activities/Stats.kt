package com.motionapps.sensortemplate.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.SparseArray
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.motionapps.sensortemplate.R
import java.util.*


class Stats : AppCompatActivity() {

    private val toHour = object : ArrayList<Boolean>() { // true - change value to time, false - print value
        init {
            add(true)
        }
    }


    private var textViewSparseArray = SparseArray<TextView>() // stored textviews
    private var handler: Handler? = Handler() // handler to update values in realtime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.stats_title)

        handler!!.postDelayed(object : Runnable {
            override fun run() {
                updateStats()
                handler!!.postDelayed(this, 5000L)
            }
        }, 5000L)

        val linearLayout = findViewById<LinearLayout>(R.id.status_container)
        val statusTitles = resources.getStringArray(R.array.status_text)

        // creates vies with stats
        for (i in statusTitles.indices) {
            val view = layoutInflater.inflate(R.layout.view_status_line, linearLayout, false)

            val title = view.findViewById<TextView>(R.id.status_title)
            title.text = statusTitles[i]

            val valueText = view.findViewById<TextView>(R.id.status_value)

            updateValue(valueText, i)

            textViewSparseArray.put(i, valueText)

            linearLayout.addView(view)
        }
    }

    fun updateStats() {
        for (i in ALL_STATS.indices) {
            updateValue(textViewSparseArray.get(i), i)
        }
    }

    /**
     * @param valueText - TextView with value of stat
     * @param i - index of stat
     */
    private fun updateValue(valueText: TextView, i: Int) {
        if (toHour[i]) {
            var value = getLong(this@Stats, ALL_STATS[i], 0L)
            value = (value/1000)
            valueText.text = secondsToFormat(value)
        } else {
            val value = getFloat(this@Stats, ALL_STATS[i], 0.0F)
            valueText.text = String.format(Locale.getDefault(), "%d", value.toInt())
        }
    }

    /**
     * @param time - in seconds
     * changes seconds to hh:mm:ss format
     */
    private fun secondsToFormat(time: Long): String {

        val hours = (time / 3600).toInt()
        val minutes = (time % 3600 / 60).toInt()
        val seconds = (time % 60).toInt()

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onSupportNavigateUp(): Boolean {
        startActivity(Intent(this, Main::class.java))
        finish()
        return true
    }


    override fun onBackPressed() {
        startActivity(Intent(this, Main::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (handler != null) {
            handler!!.removeCallbacksAndMessages(null)
            handler = null
        }
    }


    @Suppress("UNCHECKED_CAST")
    companion object{

        // keys to stored preferences
        const val RUN_TIME = "run_time"

        // all keys
        val ALL_STATS: ArrayList<String> = object : ArrayList<String>() {
            init {
                add(RUN_TIME)
            }
        }

        /**
         * getters for all types from SharedPreferences
         */

        fun getInt(context: Context, key: String, default: Int): Int{
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getInt(key, default)
        }

        fun getFloat(context: Context, key: String, default: Float): Float{
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getFloat(key, default)
        }

        fun getLong(context: Context, key: String, default: Long): Long{
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getLong(key, default)
        }

        fun getString(context: Context, key: String, default: String): String? {
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getString(key, default)
        }

        fun getStringSet(context: Context, key: String, default: Set<String>): MutableSet<String>? {
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getStringSet(key, default)
        }


        /**
         * @param context
         * @param key - key of preference in SharedPreferences
         * @param o - any object - integer, float, long, string, boolean or Set<String>
         * @throws Exception - wrong type passed to SharedPreferences
         */

        fun writePreference(context: Context, key: String, o: Any){
            val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor: SharedPreferences.Editor = sharedPreferences.edit()

            when (o) {
                is Int -> {
                    editor.putInt(key, o)
                }
                is Float -> {
                    editor.putFloat(key, o)
                }
                is Long -> {
                    editor.putLong(key, o)
                }
                is Boolean -> {
                    editor.putBoolean(key, o)
                }
                is String -> {
                    editor.putString(key, o)
                }
                is Set<*> -> {
                    editor.putStringSet(key, o as Set<String>)
                }
                else -> {
                    throw Exception("Wrong type passed to SharedPreferences in stats")
                }
            }
            editor.apply()
        }
    }
}
