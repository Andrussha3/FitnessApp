package com.example.fitnesapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fitnesapp.domain.model.Gender
import com.example.fitnesapp.domain.model.UserGoal

@Entity(tableName = "user_profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val gender: Gender,
    val age: Int?,
    val heightCm: Int?,
    val weightKg: Double?,
    val goal: UserGoal = UserGoal.MAINTAIN,
    val goalNote: String = "",
    val updatedAt: Long
)
