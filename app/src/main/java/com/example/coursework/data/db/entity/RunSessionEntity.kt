package com.example.coursework.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// One row per completed run. Weather is stored inline because there's only ever one
// snapshot per run, a separate table would just add a join.
@Entity(
    tableName = "run_sessions",
    foreignKeys = [
        ForeignKey(
            entity = RunTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["runTypeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("runTypeId")]
)
data class RunSessionEntity(
    @PrimaryKey(autoGenerate = true) val runId: Long = 0,
    val runTypeId: Long,
    val durationSeconds: Long,
    val totalDistanceMeters: Float,
    val timestamp: Long,
    // Nullable so the run still saves when the weather API call fails or the device is offline.
    val temperatureC: Double?,
    val conditionText: String?,
    val windSpeed: Double?,
    val humidity: Double?
)
