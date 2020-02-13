package com.motionapps.sensortemplate.activities

import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.motionapps.sensortemplate.activities.about.About
import com.motionapps.sensortemplate.R
import com.motionapps.sensortemplate.activities.welcome.Welcome
import com.motionapps.sensortemplate.service.DetectionService


class Main : AppCompatActivity(), View.OnClickListener {


    private var mBound: Boolean = false // activity bound to service
    private var bAnimation: Boolean = false  // animation in progress
    private var bRegistered: Boolean = false // broadcast receiver
    private lateinit var detectionService: DetectionService // main service to connect


    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val action = p1?.action
            if(p1 != null){
                when(action){
                    DetectionService.OFF_UI ->{
                        setOffUI()
                    }

                    DetectionService.ON_UI ->{
                        setOnUI()
                    }
                }
            }
        }
    }

    private val connection: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as DetectionService.DetectionServiceBinder
            detectionService = binder.getService() // get connected service
            mBound = true

            if (detectionService.running) {
                setOnUI()
            }
            unbindService(this)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
             mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if(!preferences.getBoolean(Options.FIRST_START, false)){
            finish()
            startActivity(Intent(this, Welcome::class.java))
        }

        findViewById<ImageButton>(R.id.main_button_monitoring).setOnLongClickListener {

            if(mBound){
                if (detectionService.running){
                    setOffService()
                    return@setOnLongClickListener true
                }
            }
            setOnService()
            return@setOnLongClickListener true
        }

        findViewById<ImageButton>(R.id.main_button_info).setOnClickListener(this)
        findViewById<ImageButton>(R.id.main_button_stats).setOnClickListener(this)
        findViewById<ImageButton>(R.id.main_button_options).setOnClickListener(this)

        registerBroadcast()
    }

    override fun onStart() {
        super.onStart()
        if (!mBound) {
            Intent(this, DetectionService::class.java).also { intent ->
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
            } // try to bind to service
        }
    }


    private fun registerBroadcast(){
        if(!bRegistered){
            val intentFilter = IntentFilter(DetectionService.OFF_UI)
            intentFilter.addAction(DetectionService.ON_UI)
            registerReceiver(broadcastReceiver, intentFilter)
            bRegistered = true
        }
    }

    private fun unregisterBroadcast(){
        if(bRegistered){
            unregisterReceiver(broadcastReceiver)
            bRegistered = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterBroadcast()
    }

    private fun setOnService(){
        val intentService = Intent(applicationContext, DetectionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intentService)
        }else{
            startService(intentService)
        }
        Intent(this, DetectionService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun setOffService(){
        sendBroadcast(Intent(DetectionService.STOP_SERVICE))
    }

    // change of activity
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.main_button_info -> {
                    startActivity(Intent(this, About::class.java))
                }

                R.id.main_button_stats -> {
                    startActivity(Intent(this, Stats::class.java))
                }

                R.id.main_button_options -> {
                    startActivity(Intent(this, Options::class.java))
                }
            }
        }
    }

    private fun setOnUI(){
        animateMainButton(R.drawable.main_button_on)
        findViewById<TextView>(R.id.main_text_status_value).setText(
            R.string.on
        )
    }

    private fun setOffUI(){
        animateMainButton(R.drawable.main_button_off)
        findViewById<TextView>(R.id.main_text_status_value).setText(
            R.string.off
        )
    }

    private fun animateMainButton(id: Int) {
        Log.i(TAG, "animateMainButton: started")

        if (bAnimation) {
            return
        }

        bAnimation = true
        val outAnimation = AnimationUtils.loadAnimation(this,
            R.anim.fade_out
        )

        val inAnimation = AnimationUtils.loadAnimation(this,
            R.anim.fade_in
        )

        outAnimation.setAnimationListener(object : Animation.AnimationListener {

            // Other callback methods omitted for clarity.

            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {

                // Modify the resource of the ImageButton
                findViewById<ImageButton>(R.id.main_button_monitoring).setImageResource(id)
                findViewById<ImageButton>(R.id.main_button_monitoring).startAnimation(inAnimation)
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })

        inAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                bAnimation = false
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })

        findViewById<ImageButton>(R.id.main_button_monitoring).startAnimation(outAnimation)
    }

    companion object {
        private const val TAG: String = "Main_Activity"
    }

}
