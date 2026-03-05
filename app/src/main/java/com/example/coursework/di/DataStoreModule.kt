package com.example.coursework.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val USER_PREFERENCES_NAME = "user_preferences"

// Hilt module that provides app-wide DataStore dependencies.
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    // Creates a single Preferences DataStore instance for the whole app.
    // DataStore should be a singleton to avoid multiple instances writing the same file.
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            // Backs DataStore with /files/datastore/user_preferences.preferences_pb.
            produceFile = { context.preferencesDataStoreFile(USER_PREFERENCES_NAME) }
        )
    }
}
