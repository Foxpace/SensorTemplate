package com.motionapps.sensortemplate.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.motionapps.sensortemplate.activities.about.About
import com.motionapps.sensortemplate.activities.components.Permissions
import com.motionapps.sensortemplate.R
import com.motionapps.sensortemplate.service.DetectionService


class Main : AppCompatActivity(), DetectionService.OnServiceChange, View.OnClickListener {

    private val TAG: String = "Main_Activity"
    private var mBound: Boolean = false // activity binded to service
    private var bAnimation: Boolean = false  // animation in progress
    private lateinit var detectionService: DetectionService // main service to connect

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as DetectionService.DetectionServiceBinder
            detectionService = binder.getService() // get conncected service
            detectionService.onServiceChange = this@Main // registering callback to service
            mBound = true

            if (detectionService.running) {
                setOnUI()
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
             mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        Permissions.getLocationPermission(this@Main)
    }

    override fun onStart() {
        super.onStart()
        Intent(this, DetectionService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        } // try to bind to service
    }

    override fun onStop() {
        super.onStop()
        if (mBound){
            unbindService(connection)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // location service is only needed for functionality - need to change if there are more permissions
        if (requestCode == Permissions.LOCATION_ID && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Permissions.getLocationPermission(this@Main)
            Toast.makeText(this@Main, R.string.main_location_permission, Toast.LENGTH_SHORT).show()
        }
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
        unbindService(connection)
        mBound = false
    }


    override fun onChange(running: Boolean) {
        if (running){
            setOnUI()
        }else{
            setOffUI()
        }
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

}
