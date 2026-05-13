package com.example.coursework.domain.repository

import com.example.coursework.domain.model.WeatherSnapshot

// Fetches current weather conditions for a given location.
interface WeatherRepository {
    suspend fun getWeatherAtLocation(lat: Double, lng: Double):
            WeatherSnapshot?
}