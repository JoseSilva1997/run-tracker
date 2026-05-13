package com.example.coursework.domain.repository

import kotlinx.coroutines.flow.Flow

// Persistent user settings that survive app restarts.
interface UserPreferencesRepository {
    val lastSelectedRunTypeName: Flow<String?>
    suspend fun saveLastSelectedRunType(name: String)
}