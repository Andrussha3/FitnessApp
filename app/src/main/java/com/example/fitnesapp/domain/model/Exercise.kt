package com.example.fitnesapp.domain.model

data class Exercise(
    val id: Long = 0L,
    val name: String,
    val muscleGroup: String = "",
    val muscleGroupTag: MuscleGroup = MuscleGroup.OTHER,
    val type: ExerciseType = ExerciseType.STRENGTH,
    val description: String = "",
    val loadUnit: LoadUnit = LoadUnit.KG,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val isArchived: Boolean = false,
    val isSystem: Boolean = false,
    val isUsedInPlan: Boolean = false
)
