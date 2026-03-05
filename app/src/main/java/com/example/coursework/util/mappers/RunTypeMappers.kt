package com.example.coursework.util.mappers

import com.example.coursework.data.db.entity.RunTypeEntity
import com.example.coursework.domain.model.RunType

/* 
    Centralized RunType mapping helpers between data (Room) and domain layers.
*/

// Maps Room entity -> domain model so upper layers avoid DB-specific types.
fun RunTypeEntity.toDomain() = RunType(
    id = id,
    name = name,
    targetDistanceMeters = targetDistanceMeters
)

// Maps domain model -> Room entity for persistence operations.
fun RunType.toEntity() = RunTypeEntity(
    id = id,
    name = name,
    targetDistanceMeters = targetDistanceMeters
)
