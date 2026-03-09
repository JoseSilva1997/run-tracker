package com.example.coursework.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "run_sessions",
    foreignKeys = [
        ForeignKey(
            entity = RunTypeEntity::class,
            parentColumns = ["id"], // Replace "id" with the actual primary key name of RunTypeEntity
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
    // Weather fields (nullable because network fetch might fail)
    val temperatureC: Double?,
    val conditionText: String?,
    val windSpeed: Double?,
    val humidity: Double?
)
