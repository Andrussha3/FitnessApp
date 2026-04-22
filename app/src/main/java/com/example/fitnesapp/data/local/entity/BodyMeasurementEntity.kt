package com.example.fitnesapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val measuredAt: Long,
    val bodyWeightKg: Double?,
    val neckCm: Double?,
    val shouldersCm: Double?,
    val chestCm: Double?,
    val waistCm: Double?,
    val bellyCm: Double?,
    val hipsCm: Double?,
    val glutesCm: Double?,
    val bicepsCm: Double?,
    val forearmCm: Double?,
    val thighCm: Double?,
    val calfCm: Double?,
    val note: String
)
