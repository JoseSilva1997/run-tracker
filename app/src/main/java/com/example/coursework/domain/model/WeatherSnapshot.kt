package com.example.coursework.domain.model

// Weather conditions captured once at the start of a run.
data class WeatherSnapshot(
    val temperatureC: Double,
    val conditionText: String,
    val windSpeed: Double,
    val humidity: Double
)