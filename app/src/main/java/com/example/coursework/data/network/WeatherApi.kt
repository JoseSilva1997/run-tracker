package com.example.coursework.data.network

import com.example.coursework.data.network.dto.WeatherResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit definition for the OpenWeather current-weather endpoint
 */
interface WeatherApi {
    // Fetches the current weather at the given coordinates. suspend (rather than Call<T>) so the request
    // cooperates with coroutine cancellation — if the run ends before the response arrives, the call is
    // dropped cleanly.
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric" // Defaults to metric so temperatures come back in
    ): WeatherResponseDto // Celsius and wind speed in m/s, matching the units used everywhere else in the app.
}
