package com.example.fitnesapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fitnesapp.domain.model.ExerciseType
import com.example.fitnesapp.domain.model.LoadUnit
import com.example.fitnesapp.domain.model.MuscleGroup

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val muscleGroup: String,
    val muscleGroupTag: MuscleGroup = MuscleGroup.OTHER,
    val type: ExerciseType,
    val description: String,
    val loadUnit: LoadUnit,
    val createdAt: Long,
    val updatedAt: Long,
    val isSystem: Boolean = false,
    val isArchived: Boolean = false
)
