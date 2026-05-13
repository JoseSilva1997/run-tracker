package com.example.coursework.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// A user-defined run category. Name has a unique index so duplicates are rejected at
// the DB level rather than only in the UI.
@Entity(
    tableName = "run_type",
    indices = [Index(value = ["name"], unique = true)]
)
data class RunTypeEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val targetDistanceMeters: Float,
    val isArchived: Boolean = false
)


