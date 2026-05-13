package com.example.coursework.domain.model

// A completed run: who it belongs to, how long it took, and the weather at start.
data class RunSession(
    val id: Long = 0,
    val runTypeId: Long,
    val durationSeconds: Long,
    val totalDistanceMeters: Float,
    val timestamp: Long,
    val weatherSnapshot: WeatherSnapshot?
)