package com.motionapps.sensormodel.components.gps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*

/**
 * class for GPS management
 * @param context
 * @param callBack - callback for location
 * @param bLastLocation - if last location is needed
 */

@SuppressLint("MissingPermission")
class GPSCallback constructor(context: Context?, private val callBack: OnLocationChangedCallback,
    bLastLocation: Boolean) : LocationCallback() {



    interface OnLocationChangedCallback {
        fun onLocationChanged(location: Location?)
        fun onLastLocationSuccess(location: Location?)
        fun onAvailabilityChanged(locationAvailability: LocationAvailability?)
    }

    interface OnLastLocation {
        fun onLastLocation(location: Location?)
    }

    private val TAG = "GPS_location"

    private var locationAvailability: LocationAvailability? = null
    private val locationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)

    private var lastLocation: Location? = null
    private var gpsParameters: GPSParameters? = null
    private var registered = false

    init {
        // klient na volanie GPS
        if (bLastLocation) {
            locationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    lastLocation = location
                    callBack.onLastLocationSuccess(location)
                }.addOnFailureListener {
                    callBack.onLastLocationSuccess(null)
                }
        }
    }

    fun setGpsParameters(gpsParameters: GPSParameters?) {
        if (registered) {
            gpsOff()
        }
        this.gpsParameters = gpsParameters
    }

    /**
     * @param locationResult - response of GPS - we want only last one
     */
    override fun onLocationResult(locationResult: LocationResult) {
        super.onLocationResult(locationResult)
        if (locationResult.locations.size > 0) {
            lastLocation = locationResult.lastLocation
            if (lastLocation != null) {
                callBack.onLocationChanged(lastLocation)
            }
        }
    }

    /**
     * @param locationAvailability - change of availbility
     */
    override fun onLocationAvailability(locationAvailability: LocationAvailability) {
        super.onLocationAvailability(locationAvailability)
        this.locationAvailability = locationAvailability
        callBack.onAvailabilityChanged(locationAvailability)
    }

    fun gpsOff() {
        Log.i(TAG, "Logging off location")
        locationClient.removeLocationUpdates(this)
        registered = false
    }

    /**
     * change of frequency of GPS - needed after new parameters were set
     */
    @SuppressLint("MissingPermission")
    fun changeRequest() {
        if (registered) {
            gpsOff()
        }
        if (gpsParameters == null) {
            return
        }
        Log.i(TAG, "Registering new request")
        locationClient.requestLocationUpdates(createRequest(), this, Looper.getMainLooper())
        registered = true
    }

    /**
     * @return LocationRequest - new request by template
     */
    private fun createRequest(): LocationRequest {
        Log.i(TAG, "Registering: " + gpsParameters.toString())
        return LocationRequest.create().apply {
            priority = gpsParameters!!.priority
            interval = gpsParameters!!.interval
            fastestInterval = gpsParameters!!.fastestInterval
            smallestDisplacement = gpsParameters!!.distance.toFloat()
        }
    }

    companion object {
        @SuppressLint("MissingPermission")
        fun getLastLocation(
            context: Context?,
            onLastLocation: OnLastLocation
        ) {
            val locationClient = LocationServices.getFusedLocationProviderClient(context!!) // call for last known location
            locationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    onLastLocation.onLastLocation(
                        location
                    )
                }
                .addOnFailureListener {
                    onLastLocation.onLastLocation(
                        null
                    )
                }
        }
    }


}