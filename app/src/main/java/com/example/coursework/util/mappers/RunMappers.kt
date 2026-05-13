package com.example.coursework.util.mappers

import com.example.coursework.data.db.entity.RunPointEntity
import com.example.coursework.data.db.entity.RunSessionEntity
import com.example.coursework.domain.model.RunPoint
import com.example.coursework.domain.model.RunSession
import com.example.coursework.domain.model.WeatherSnapshot

/**
 * Conversion functions between the Room entities and the domain models.
 * Kept as top-level extension functions so neither layer has to know about the other.
 * The DB classes stay free of domain types and vice versa.
 */


//  Flattens the optional WeatherSnapshot into individual nullable columns on the entity,
//  since Room stores primitives rather than nested objects.
fun RunSession.toEntity(): RunSessionEntity {
    return RunSessionEntity(
        runId = this.id,
        runTypeId = this.runTypeId,
        durationSeconds = this.durationSeconds,
        totalDistanceMeters = this.totalDistanceMeters,
        timestamp = this.timestamp,
        temperatureC = this.weatherSnapshot?.temperatureC,
        conditionText = this.weatherSnapshot?.conditionText,
        windSpeed = this.weatherSnapshot?.windSpeed,
        humidity = this.weatherSnapshot?.humidity
    )
}

// Takes the parent session id as a parameter because a RunPoint in the domain layer doesn't know
// which run it belongs to until the run row has been inserted and assigned an id.
fun RunPoint.toEntity(runSessionId: Long): RunPointEntity {
    return RunPointEntity(
        runSessionId = runSessionId,
        latitude = this.latitude,
        longitude = this.longitude,
        timestamp = this.timestamp,
        accuracy = this.accuracy
    )
}

// Rebuilds the WeatherSnapshot only when every weather column is present.
// If any one is null the snapshot is treated as missing, so the UI doesn't have to render partial weather data.
fun RunSessionEntity.toDomain(): RunSession {
    val weatherSnapshot = if (temperatureC != null && conditionText != null && windSpeed != null && humidity != null) {
        WeatherSnapshot(temperatureC, conditionText, windSpeed, humidity)
    } else null

    return RunSession(
        id = this.runId,
        runTypeId = this.runTypeId,
        durationSeconds = this.durationSeconds,
        totalDistanceMeters = this.totalDistanceMeters,
        timestamp = this.timestamp,
        weatherSnapshot = weatherSnapshot
    )
}
