package com.example.coursework.di

import com.example.coursework.data.repository.RunRepositoryImpl
import com.example.coursework.data.repository.RunTypeRepositoryImpl
import com.example.coursework.data.repository.UserPreferencesRepositoryImpl
import com.example.coursework.data.repository.WeatherRepositoryImpl
import com.example.coursework.domain.repository.RunRepository
import com.example.coursework.domain.repository.RunTypeRepository
import com.example.coursework.domain.repository.UserPreferencesRepository
import com.example.coursework.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Hilt module that maps repository interfaces to their concrete implementations.
// This keeps ViewModels dependent on abstractions, not concrete classes.
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRunTypeRepository(
        impl: RunTypeRepositoryImpl
    ): RunTypeRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindRunRepository(
        impl: RunRepositoryImpl
    ): RunRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        impl: WeatherRepositoryImpl
    ): WeatherRepository
}
