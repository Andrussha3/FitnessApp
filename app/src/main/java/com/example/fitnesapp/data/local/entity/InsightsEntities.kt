package com.example.fitnesapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.fitnesapp.domain.model.PersonalRecordType
import com.example.fitnesapp.domain.model.RecommendationAction

@Entity(tableName = "progression_recommendations", indices = [Index("sessionId"), Index("exerciseId")])
data class ProgressionRecommendationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: Long,
    val exerciseId: Long?,
    val exerciseName: String,
    val action: RecommendationAction,
    val currentWeight: Double?,
    val suggestedWeight: Double?,
    val message: String
)

@Entity(tableName = "personal_records", indices = [Index("sessionId"), Index("exerciseId")])
data class PersonalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: Long,
    val exerciseId: Long?,
    val exerciseName: String,
    val recordType: PersonalRecordType,
    val value: Double,
    val achievedAt: Long
)
