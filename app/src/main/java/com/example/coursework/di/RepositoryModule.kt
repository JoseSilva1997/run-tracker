package com.example.coursework.di

import com.example.coursework.domain.repository.RunTypeRepository
import com.example.coursework.domain.repository.RunTypeRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRunTypeRepository(
        impl: RunTypeRepositoryImpl
    ): RunTypeRepository {
        return impl
    }
}
