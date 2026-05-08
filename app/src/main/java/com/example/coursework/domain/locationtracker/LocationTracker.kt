package com.example.coursework.domain.locationtracker

import com.example.coursework.domain.model.RunPoint
import kotlinx.coroutines.flow.Flow

interface LocationTracker {
    fun getLocationUpdates(intervalMs: Long): Flow<RunPoint>

    // Cached last-known fix from the OS. Returns null if no cached fix
    // exists yet (cold device, denied permission, etc.). Used to seed
    // the map view so it doesn't open at lat/lng (0,0).
    suspend fun getLastKnownLocation(): RunPoint?
}