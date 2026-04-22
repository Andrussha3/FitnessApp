package com.example.fitnesapp.domain.model

data class Note(
    val id: Long = 0L,
    val title: String,
    val text: String,
    val workoutDate: Long? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
