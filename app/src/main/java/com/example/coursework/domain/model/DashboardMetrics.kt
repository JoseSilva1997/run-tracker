package com.example.coursework.domain.model

// Aggregated stats shown on the dashboard cards.
data class DashboardMetrics(
    val avgPaceSecPerKM: Int?,
    val bestTimeSeconds: Long?,
    val weeklyDistanceMeters: Float,
    val trendPct: Float?
)
