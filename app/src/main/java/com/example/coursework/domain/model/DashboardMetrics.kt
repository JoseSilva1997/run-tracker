package com.example.coursework.domain.model

data class DashboardMetrics(
    val avgPaceSecPerKM: Int?,
    val bestTimeSeconds: Long?,
    val weeklyDistanceMeters: Float,
    val trendPct: Float?
)
