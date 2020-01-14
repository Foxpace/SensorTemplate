package com.motionapps.sensortemplate.activities.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.judemanutd.autostarter.AutoStartPermissionHelper
import com.motionapps.sensortemplate.R

object Permissions {

    const val LOCATION_ID = 1568

    /**
     * permission for location
     */
    fun getLocationPermission(activity: Activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_ID
            )
        }
    }

    /**
     * can check and open settings of white list, when app is not allowed for background processing
     */
    fun getBackgroundPermission(context: Context) {
        if(!AutoStartPermissionHelper.getInstance().isAutoStartPermissionAvailable(context)){
            Toast.makeText(context, context.getString(R.string.white_list), Toast.LENGTH_LONG).show()
            AutoStartPermissionHelper.getInstance().getAutoStartPermission(context)
        }
    }

    fun openWhiteList(context: Context) {
        AutoStartPermissionHelper.getInstance().getAutoStartPermission(context)
    }
}