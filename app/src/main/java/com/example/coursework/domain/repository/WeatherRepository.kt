package com.example.coursework.domain.repository

import com.example.coursework.domain.model.WeatherSnapshot

interface WeatherRepository {
    suspend fun getWeatherAtLocation(lat: Double, lng: Double):
            WeatherSnapshot?
}