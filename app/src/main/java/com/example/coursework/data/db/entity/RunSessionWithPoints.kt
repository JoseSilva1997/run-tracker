// Room does not natively return a class containing a list of another class
// Using a POJO to tell Room how to map the 1-to-many relationship between a Run and its Points.
package com.example.coursework.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class RunSessionWithPoints(
    @Embedded val runSession: RunSessionEntity,
    @Relation(
        parentColumn = "runId",
        entityColumn = "runSessionId"
    )
    val points: List<RunPointEntity>
)
