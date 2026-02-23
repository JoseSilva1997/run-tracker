package com.example.coursework.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val lastSelectedRunTypeName: Flow<String?>
    suspend fun saveLastSelectedRunType(name: String)
}