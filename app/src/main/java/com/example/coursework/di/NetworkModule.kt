package com.example.coursework.di

import com.example.coursework.BuildConfig
import com.example.coursework.data.network.WeatherApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

// Hilt module that wires up the network stack. @Provides rather than @Binds because we
// don't own Retrofit/OkHttp, so Hilt needs explicit factory functions for them.
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    const val OPEN_WEATHER_MAP_BASE_URL = "https://api.openweathermap.org/"

    // Builds a single Retrofit instance reused by every API binding below.
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        // Full body logging only in debug. Release builds stay silent so the
        // OpenWeather API key (sent as a query param) never lands in Logcat.
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        // Add the interceptor to the client
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(OPEN_WEATHER_MAP_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Generates the WeatherApi implementation Retrofit makes from the interface.
    @Provides
    @Singleton
    fun provideWeatherApi(retrofit: Retrofit): WeatherApi {
        return retrofit.create(WeatherApi::class.java)
    }
}