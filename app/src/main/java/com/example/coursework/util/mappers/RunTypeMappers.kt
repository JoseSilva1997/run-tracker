package com.example.coursework.util.mappers

import com.example.coursework.data.db.entity.RunTypeEntity
import com.example.coursework.domain.model.RunType

fun RunTypeEntity.toDomain() = RunType(
    id = id,
    name = name,
    targetDistanceMeters = targetDistanceMeters
)

fun RunType.toEntity() = RunTypeEntity(
    id = id,
    name = name,
    targetDistanceMeters = targetDistanceMeters
)