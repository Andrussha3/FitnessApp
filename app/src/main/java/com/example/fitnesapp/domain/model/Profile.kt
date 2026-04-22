package com.example.fitnesapp.domain.model

data class UserProfile(
    val name: String = "",
    val gender: Gender = Gender.UNSPECIFIED,
    val age: Int? = null,
    val heightCm: Int? = null,
    val weightKg: Double? = null,
    val goal: UserGoal = UserGoal.MAINTAIN,
    val goalNote: String = "",
    val updatedAt: Long = 0L
)
