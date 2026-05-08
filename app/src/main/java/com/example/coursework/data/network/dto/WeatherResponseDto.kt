package com.example.coursework.data.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for OpenWeatherMap Current Weather API response.
 * Note: Temperature is returned in Kelvin by default; use units=metric for Celsius.
 */
data class WeatherResponseDto(
    @SerializedName("main") val main: MainDto,
    @SerializedName("weather") val weather: List<WeatherDescriptionDto>,
    @SerializedName("wind") val wind: WindDto,
    @SerializedName("name") val cityName: String
)

data class MainDto(
    @SerializedName("temp") val temp: Double,
    @SerializedName("humidity") val humidity: Double
)

data class WeatherDescriptionDto(
    @SerializedName("description") val description: String // e.g., "broken clouds"
)

data class WindDto(
    @SerializedName("speed") val speed: Double
)