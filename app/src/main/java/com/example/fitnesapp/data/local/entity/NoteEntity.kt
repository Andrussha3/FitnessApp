package com.example.fitnesapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val text: String,
    val workoutDate: Long?,
    val createdAt: Long,
    val updatedAt: Long
)
