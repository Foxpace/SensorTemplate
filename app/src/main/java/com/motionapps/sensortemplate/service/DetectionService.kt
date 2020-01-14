package com.motionapps.sensortemplate.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import com.motionapps.sensortemplate.activities.components.Notify
import com.motionapps.sensortemplate.activities.Stats
import com.motionapps.sensortemplate.model.Model
import com.motionapps.sensortemplate.R


class DetectionService : Service() {

    var running: Boolean = false // indicates active service
    private var serviceId: Int = 4578

    var onServiceChange: OnServiceChange?= null // callback to main activity

    private var receiverRegistered: Boolean = false
    private lateinit var receiver: DetectionBroadcastReceiver

    private var model: Model ?= null // main processing unit for analysis


    override fun onBind(p0: Intent?): IBinder? {
        return DetectionServiceBinder()
    }

    private val startTime: Long = System.currentTimeMillis()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        running = true

        model = Model(this@DetectionService)

        registerBroadcastReceiver()

        if (onServiceChange != null){
            onServiceChange!!.onChange(true)
        }

        model!!.onStart()

        startForeground(serviceId, Notify.createForegroundNotification(this,
            getString(R.string.notification_header), getString(R.string.notification_body)))

        return super.onStartCommand(intent, flags, startId)
    }

    private fun registerBroadcastReceiver(){
        receiver = DetectionBroadcastReceiver(this, model!!) // creates all the filters

        if(!receiverRegistered){
            registerReceiver(receiver, receiver.intentFilter)
            receiverRegistered = true
        }
    }

    private fun unregisterBroadcastReceiver(){
        if(receiverRegistered){
            unregisterReceiver(receiver)
            receiverRegistered = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if(!running){
            return
        }

        unregisterBroadcastReceiver()

        model?.onDestroy()

        // example of stat update - can be moved anywhere in code, where context is available
        Stats.writePreference(this@DetectionService, Stats.RUN_TIME,
            Stats.getLong(this@DetectionService, Stats.RUN_TIME, 0L) +
                    System.currentTimeMillis() - startTime)

        running = false
    }

    fun onStopIntent(){
        if (onServiceChange != null){
            onServiceChange!!.onChange(false)
            Notify.cancelNotification(this@DetectionService, serviceId)
        }
        stopSelf()
    }


    inner class DetectionServiceBinder : Binder() {
        fun getService(): DetectionService = this@DetectionService
    }

    /**
     * actions consist all custom intents for service
     */
    companion object Actions{

        const val STOP_SERVICE: String = "STOP_DETECTION_SERVICE" // comes from main activity/ notification

        private val INTENTS: Array<String> = arrayOf(STOP_SERVICE)

        fun registerIntents(intentFilter: IntentFilter){
            for(s in INTENTS){
                intentFilter.addAction(s)
            }
        }
    }

    /**
     *  interface for callback to main activity
     */
    interface OnServiceChange {
        fun onChange(running: Boolean)
    }

}