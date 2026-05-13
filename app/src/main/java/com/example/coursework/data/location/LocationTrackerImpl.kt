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
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Wraps the Fused Location API and exposes it through clean Kotlin coroutines.
 */
class LocationTrackerImpl @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationTracker {

    // Streams GPS fixes as RunPoints for as long as the caller keeps collecting.
    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(intervalMs: Long): Flow<RunPoint> {
        // callbackFlow is used here instead of plain flow because the location API delivers fixes through a
        // callback, not a suspending call — callbackFlow is what bridges that callback world into Flow
        // semantics.
        return callbackFlow {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                intervalMs
            ).build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    // The OS can deliver several fixes in one batch when it has been holding them.
                    // Only the newest one matters for live tracking, so we take the last.
                    result.locations.lastOrNull()?.let { location ->
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
            // Runs when the collector cancels or the flow completes, deregistering the location
            // callback so the Fused client stops sending updates and doesn't leak.
            awaitClose {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
    }

    // Returns the device's last cached fix (or null if there isn't one),
    // bridging the Play Services Task API into a one-shot suspend call.
    @SuppressLint("MissingPermission")
    override suspend fun getLastKnownLocation(): RunPoint? = suspendCancellableCoroutine { cont ->
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                val point = location?.let {
                    RunPoint(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        accuracy = it.accuracy,
                        timestamp = it.time
                    )
                }
                cont.resume(point)
            }
            .addOnFailureListener { cont.resume(null) }
    }
}