@file:Suppress("DEPRECATION")

package com.motionapps.sensortemplate.model.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager


class BatteryOptimizer {

    private var wakelock: PowerManager.WakeLock? = null

    /**
     * @param context
     * @param time - time after which partial wakelock will be released - in ms
     */
    fun setWakeLockTime(context: Context, time: Long){
        wakelock = (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Template:WakeLock").apply {
                acquire(time)
            }
        }
    }

    /**
     * @param context
     * wakelock is locked till the app/system will release him
     */

    @SuppressLint("WakelockTimeout")
    fun setWakeLockInf(context: Context){
        wakelock = (context.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Template:WakeLock").apply {
                acquire()
            }
        }
    }

    fun turnOffWakeLock() {
        if(wakelock != null){
            if(wakelock!!.isHeld){
                wakelock!!.release()
            }else{
                wakelock = null
            }
        }
    }

    companion object{
        // constanst for connection type
        const val NO_CONNECTION = 0
        const val MOBILE = 1
        const val WIFI = 2

        fun getNetworkString(int: Int): String{
            return when(int){
                NO_CONNECTION -> "NO CONNECTION"
                MOBILE -> "MOBILE"
                WIFI -> "WIFI"
                else -> "UNKNOWN"
            }
        }

     /**
      * @param context
      * @return - integer 2 - WIFI, 1 - MOBILE, 0 - NO CONNECTION
      * method is adapted for android 4.4.4. - Android 10
      */

    fun getConnectionType(context: Context): Int {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nc: NetworkCapabilities? = cm.getNetworkCapabilities(cm.activeNetwork)
            if (nc != null) {
                if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return MOBILE
                } else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return WIFI
                }
            }
        } else {
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            if (activeNetwork != null) { // connected to the internet
                if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                    return WIFI
                } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                    return MOBILE
                }
            }
        }
        return NO_CONNECTION
    }

        /**
         * functions to register other system intents
         */
        fun registerBatteryChangeState(intentFilter: IntentFilter) {
            intentFilter.addAction(Intent.ACTION_BATTERY_OKAY)
            intentFilter.addAction(Intent.ACTION_BATTERY_LOW)
        }

        fun registerNetworkChange(intentFilter: IntentFilter){
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        }

        fun registerScreenChange(intentFilter: IntentFilter) {
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF)
            intentFilter.addAction(Intent.ACTION_SCREEN_ON)
        }

        fun registerChargingChange(intentFilter: IntentFilter) {
            intentFilter.addAction(Intent.ACTION_POWER_CONNECTED)
            intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
    }


}

