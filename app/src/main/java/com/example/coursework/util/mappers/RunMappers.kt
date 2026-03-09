package com.example.coursework.util.mappers

import com.example.coursework.data.db.entity.RunPointEntity
import com.example.coursework.data.db.entity.RunSessionEntity
import com.example.coursework.domain.model.RunPoint
import com.example.coursework.domain.model.RunSession
import com.example.coursework.domain.model.WeatherSnapshot

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

fun RunPoint.toEntity(runSessionId: Long): RunPointEntity {
    return RunPointEntity(
        runSessionId = runSessionId,
        latitude = this.latitude,
        longitude = this.longitude,
        timestamp = this.timestamp,
        accuracy = this.accuracy
    )
}

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
