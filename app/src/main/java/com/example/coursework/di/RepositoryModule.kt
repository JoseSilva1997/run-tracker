package com.example.coursework.di

import com.example.coursework.domain.repository.RunTypeRepository
import com.example.coursework.data.repository.RunTypeRepositoryImpl
import com.example.coursework.domain.repository.UserPreferencesRepository
import com.example.coursework.data.repository.UserPreferencesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Hilt module that maps repository interfaces to their concrete implementations.
// This keeps ViewModels dependent on abstractions, not concrete classes.
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // When RunTypeRepository is requested, provide RunTypeRepositoryImpl.
    @Binds
    abstract fun bindRunTypeRepository(
        impl: RunTypeRepositoryImpl
    ): RunTypeRepository

    // When UserPreferencesRepository is requested, provide UserPreferencesRepositoryImpl.
    @Binds
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository
}
