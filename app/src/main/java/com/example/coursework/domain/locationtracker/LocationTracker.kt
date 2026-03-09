package com.example.coursework.domain.locationtracker

import com.example.coursework.domain.model.LocationPoint
import kotlinx.coroutines.flow.Flow

interface LocationTracker {
    fun getLocationUpdates(intervalMs: Long): Flow<LocationPoint>
}