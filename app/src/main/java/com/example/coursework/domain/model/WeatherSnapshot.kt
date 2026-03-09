package com.example.coursework.domain.model

data class WeatherSnapshot(
    val temperatureC: Double,
    val conditionText: String,
    val windSpeed: Double,
    val humidity: Double
)