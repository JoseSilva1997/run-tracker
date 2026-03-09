package com.example.coursework.data.repository

import com.example.coursework.BuildConfig
import com.example.coursework.data.network.WeatherApi
import com.example.coursework.domain.model.WeatherSnapshot
import com.example.coursework.domain.repository.WeatherRepository
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApi
) : WeatherRepository {

     val openWeatherApiKey = BuildConfig.OPEN_WEATHER_API_KEY

    override suspend fun getWeatherAtLocation(lat: Double, lng: Double): WeatherSnapshot? {
        return try {
            val response = weatherApi.getCurrentWeather(
                lat,
                lng,
                apiKey = openWeatherApiKey
            )

            WeatherSnapshot(
                temperatureC = response.main.temp,
                conditionText = response.weather.firstOrNull()?.description?: "Unknown",
                windSpeed = response.wind.speed,
                humidity = response.main.humidity
            )
        } catch (e: Exception) {
            // If offline, timeout, or API failure, return null safely.
            null
        }
    }
}