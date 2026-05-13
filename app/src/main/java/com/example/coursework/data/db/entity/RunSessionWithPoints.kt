package com.example.coursework.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation

// POJO that tells Room how to map the 1-to-many relationship between a run and its
// GPS points, since Room can't return a nested list directly from a query.
data class RunSessionWithPoints(
    @Embedded val runSession: RunSessionEntity,
    @Relation(
        parentColumn = "runId",
        entityColumn = "runSessionId"
    )
    val points: List<RunPointEntity>
)
