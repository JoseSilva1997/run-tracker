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

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

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

    @Provides
    fun provideRunTypeDao(database: AppDatabase): RunTypeDao {
        return database.runTypeDao()
    }

    // Exposes the new RunSessionDao
    @Provides
    fun provideRunSessionDao(database: AppDatabase): RunSessionDao {
        return database.runSessionDao()
    }
}
