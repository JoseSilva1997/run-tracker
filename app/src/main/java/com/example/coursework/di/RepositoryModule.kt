package com.example.coursework.di

import com.example.coursework.domain.repository.RunTypeRepository
import com.example.coursework.domain.repository.RunTypeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindRunTypeRepository(
        impl: RunTypeRepositoryImpl
    ): RunTypeRepository
}
