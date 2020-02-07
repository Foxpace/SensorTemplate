package com.motionapps.sensortemplate.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.ActivityTransitionResult
import com.motionapps.sensormodel.components.ActivityDetectionManager
import com.motionapps.sensormodel.components.BatteryOptimizer
import com.motionapps.sensormodel.Model

class DetectionBroadcastReceiver(private val service: DetectionService,
                                 private val model: Model
                                ): BroadcastReceiver() {

    val intentFilter: IntentFilter = IntentFilter()

    init {
        // intent filter takes intents from Main activity and system - battery, screen, movement, ...

        DetectionService.registerIntents(intentFilter)
        ActivityDetectionManager.registerIntents(intentFilter)

        BatteryOptimizer.registerNetworkChange(intentFilter)
        BatteryOptimizer.registerChargingChange(intentFilter)
        BatteryOptimizer.registerScreenChange(intentFilter)
        BatteryOptimizer.registerBatteryChangeState(intentFilter)
    }

    /**
     * receiver aggregates intents into methods implemented mainly in model object
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action != null) {

            when(action){
                DetectionService.STOP_SERVICE -> { // intent from main activity
                    service.onStopIntent()
                }

                ActivityDetectionManager.MOVING_STATE_TRANSITION -> { // from activity API
                    if (ActivityTransitionResult.hasResult(intent)) {
                        ActivityTransitionResult.extractResult(intent)?.let { model.onTransition(it) }
                    }
                }

                ActivityDetectionManager.MOVING_STATE_UPDATES -> { // from activity API
                    if (ActivityRecognitionResult.hasResult(intent)) {
                        model.onStateUpdate(ActivityRecognitionResult.extractResult(intent))
                    }
                }

                //TODO implement other intents if needed
                Intent.ACTION_SCREEN_ON -> {

                }

                Intent.ACTION_SCREEN_OFF -> {

                }

                Intent.ACTION_POWER_CONNECTED -> {

                }

                Intent.ACTION_POWER_DISCONNECTED -> {

                }

                Intent.ACTION_BATTERY_OKAY -> { // called when battery comes back to higher values
                    model.onBatteryOK()
                }

                Intent.ACTION_BATTERY_LOW -> { // when battery is low - usually around 20 %
                    model.onLowBattery()
                }

                WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                    model.onNetworkChange()
                }
            }
        }
    }
}