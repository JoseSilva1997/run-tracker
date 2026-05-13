package com.example.coursework.data.repository

import com.example.coursework.BuildConfig
import com.example.coursework.data.network.WeatherApi
import com.example.coursework.domain.model.WeatherSnapshot
import com.example.coursework.domain.repository.WeatherRepository
import javax.inject.Inject

/**
 * Fetches current weather from the OpenWeather API and maps it into the app's WeatherSnapshot
 * domain model.
 */
class WeatherRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApi
) : WeatherRepository {

     private val openWeatherApiKey = BuildConfig.OPEN_WEATHER_API_KEY

    // Returns a WeatherSnapshot for the given coordinates, or null if the network call fails.
    // Called once per run, right after the first good GPS fix.
    override suspend fun getWeatherAtLocation(lat: Double, lng: Double): WeatherSnapshot? {
        return try {
            // OpenWeather can return several condition entries (e.g. "rain" and "drizzle" together).
            // The first one is enough for the Summary card.
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
            // Catching Exception broadly on purpose: weather is a non-essential extra and the run
            // must still save with null weather fields whether the device is offline, the request
            // times out, or the API returns a malformed response.
            null
        }
    }
}