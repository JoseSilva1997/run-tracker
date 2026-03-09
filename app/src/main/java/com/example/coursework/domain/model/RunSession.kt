package com.example.coursework.domain.model

data class RunSession(
    val id: Long = 0,
    val runTypeId: Long,
    val durationSeconds: Long,
    val totalDistanceMeters: Float,
    val timestamp: Long,
    val weatherSnapshot: WeatherSnapshot?
)