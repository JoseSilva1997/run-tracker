package com.example.coursework.di

import android.content.Context
import androidx.room.Room
import com.example.coursework.data.db.AppDatabase
import com.example.coursework.data.db.dao.RunTypeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Hilt module that provides Room database dependencies.
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Creates a single AppDatabase instance for the app process.
    // Room databases are expensive to create and should be shared.
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "run_tracker.db"
        ).build()
    }

    // Exposes RunTypeDao from the singleton AppDatabase.
    @Provides
    fun provideRunTypeDao(database: AppDatabase): RunTypeDao {
        return database.runTypeDao()
    }
}
