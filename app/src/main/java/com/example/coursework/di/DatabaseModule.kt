package com.example.coursework.di

import android.content.Context
import androidx.room.Room
import com.example.coursework.data.db.AppDatabase
import com.example.coursework.data.db.dao.RunSessionDao
import com.example.coursework.data.db.dao.RunTypeDao
import com.example.coursework.data.db.migrations.MIGRATION_3_4
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Hilt module for the Room database and its DAOs. @Provides is used because Room
// generates the database implementation, so Hilt needs explicit factory functions.
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Builds the single AppDatabase instance for the app. Registered migrations are
    // listed here so Room can step an existing user's DB up to the current schema
    // version instead of wiping it.
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "run_tracker.db"
        )
            .addMigrations(MIGRATION_3_4)
            .build()
    }

    // DAOs are not @Singleton because Room already returns the same instance from the
    // database object, so adding the scope here would just be redundant.
    @Provides
    fun provideRunTypeDao(database: AppDatabase): RunTypeDao {
        return database.runTypeDao()
    }

    @Provides
    fun provideRunSessionDao(database: AppDatabase): RunSessionDao {
        return database.runSessionDao()
    }
}
