package com.example.coursework.ui.runtypes

import com.example.coursework.domain.model.RunType

data class RunTypeUiState(
    val runTypes: List<RunType> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val clearInputsToken: Long = 0L
)
