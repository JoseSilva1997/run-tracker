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

/**
 * Persists small user preferences in DataStore.
 */
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val LAST_SELECTED_RUN_TYPE = stringPreferencesKey("last_selected_run_type")
    }
    // Exposed as a Flow so the UI re-renders automatically whenever the saved value changes,
    // instead of needing a manual re-read.
    override val lastSelectedRunTypeName: Flow<String?> = dataStore.data
        // Only IOException is swallowed — that's a real disk read failure, recoverable by falling back to
        // empty preferences. Anything else is a programming bug and is rethrown so it surfaces during
        // development instead of being hidden.
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