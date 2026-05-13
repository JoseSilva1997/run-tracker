package com.example.coursework.domain.model

// A user-defined category of run, identified by name and a target distance.
data class RunType(
    val id: Long,
    val name: String,
    val targetDistanceMeters: Float,
    val isArchived: Boolean = false
)
