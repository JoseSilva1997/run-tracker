package com.example.coursework.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.coursework.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val LAST_SELECTED_RUN_TYPE = stringPreferencesKey("last_selected_run_type")
    }

    override val lastSelectedRunTypeName: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.LAST_SELECTED_RUN_TYPE]
        }

    override suspend fun saveLastSelectedRunType(name: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SELECTED_RUN_TYPE] = name
        }
    }
}