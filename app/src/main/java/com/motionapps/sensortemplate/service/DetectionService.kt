package com.motionapps.sensortemplate.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import com.motionapps.sensortemplate.activities.components.Notify
import com.motionapps.sensortemplate.activities.Stats
import com.motionapps.sensortemplate.R
import com.motionapps.sensormodel.Model


class DetectionService : Service() {

    var running: Boolean = false // indicates active service
    private var serviceId: Int = 4578

    private var receiverRegistered: Boolean = false
    private lateinit var receiver: DetectionBroadcastReceiver

    private var model: Model?= null // main processing unit for analysis


    override fun onBind(p0: Intent?): IBinder? {
        return DetectionServiceBinder()
    }

    private val startTime: Long = System.currentTimeMillis()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        running = true

        model = Model(this@DetectionService)

        registerBroadcastReceiver()

        sendBroadcast(Intent(ON_UI))

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
        sendBroadcast(Intent(OFF_UI))
        stopForeground(true)
        stopSelf()
    }


    inner class DetectionServiceBinder : Binder() {
        fun getService(): DetectionService = this@DetectionService
    }

    /**
     * actions consist all custom intents for service
     */
    companion object Actions{

        const val STOP_SERVICE: String = "com.motionapps.STOP_DETECTION_SERVICE" // comes from main activity / notification
        const val OFF_UI: String = "com.motionapps.OFF_UI" // set off UI
        const val ON_UI: String = "com.motionapps.ON_UI" // set on UI
        private val INTENTS: Array<String> = arrayOf(STOP_SERVICE, OFF_UI, ON_UI)

        fun registerIntents(intentFilter: IntentFilter){
            for(s in INTENTS){
                intentFilter.addAction(s)
            }
        }
    }

}