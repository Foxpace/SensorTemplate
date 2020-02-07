package com.motionapps.sensormodel.components.gps

import com.google.android.gms.location.LocationRequest

/**
 * basic class for parameters and some templates - can be added
 */
enum class GPSParameters(
    val priority: Int,
    val interval: Long,
    val fastestInterval: Long,
    val distance: Int
) {
    WALK_PARAMS(LocationRequest.PRIORITY_NO_POWER, 60000L, 30000L, 100),
    CAR_PARAMS_LOW(LocationRequest.PRIORITY_HIGH_ACCURACY, 20000L, 5000L, 100),
    CAR_PARAMS_HIGH(LocationRequest.PRIORITY_HIGH_ACCURACY, 10000L, 5000L, 20);

}