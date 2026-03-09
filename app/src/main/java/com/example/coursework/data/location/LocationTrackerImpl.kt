package com.example.coursework.data.location

import android.annotation.SuppressLint
import android.os.Looper
import com.example.coursework.domain.locationtracker.LocationTracker
import com.example.coursework.domain.model.RunPoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LocationTrackerImpl(
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationTracker {

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(intervalMs: Long): Flow<RunPoint> {
        return callbackFlow {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                intervalMs
            ).build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    result.locations.lastOrNull()?.let { location ->
                        // Map the Android Location to your Domain Model
                        val point = RunPoint(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracy = location.accuracy,
                            timestamp = location.time
                        )
                        trySend(point)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            awaitClose {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
    }
}