package com.example.coursework.domain.model

// Single GPS sample recorded during a run.
data class RunPoint(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long
)
