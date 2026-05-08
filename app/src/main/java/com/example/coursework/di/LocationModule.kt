// di/LocationModule.kt
package com.example.coursework.di

import android.content.Context
import com.example.coursework.data.location.LocationTrackerImpl
import com.example.coursework.domain.locationtracker.LocationTracker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Provides the third-party FusedLocationProviderClient (no @Inject constructor available).
@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }
}

// Binds LocationTracker -> LocationTrackerImpl
@Module
@InstallIn(SingletonComponent::class)
abstract class LocationTrackerBindings {
    @Binds
    @Singleton
    abstract fun bindLocationTracker(impl: LocationTrackerImpl): LocationTracker
}
