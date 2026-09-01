package com.gigmiles.app.location

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority

class DriveLocationTracker(
    private val client: FusedLocationProviderClient,
    private val onMilesUpdated: (Double) -> Unit,
    private val onLocationUpdated: (Location) -> Unit
) {
    private var lastLocation: Location? = null
    var miles: Double = 0.0
        private set

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.locations.forEach { location ->
                onLocationUpdated(location)
                lastLocation?.let { previous ->
                    val distanceMeters = previous.distanceTo(location).toDouble()
                    if (distanceMeters >= 5.0) {
                        miles += distanceMeters / 1609.344
                        onMilesUpdated(miles)
                    }
                }
                lastLocation = location
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        lastLocation = null
        miles = 0.0
        onMilesUpdated(0.0)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateDistanceMeters(10f)
            .build()
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
    }

    fun stop() {
        client.removeLocationUpdates(callback)
    }
}
