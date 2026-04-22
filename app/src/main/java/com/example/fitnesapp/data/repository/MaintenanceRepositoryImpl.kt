package com.example.fitnesapp.data.repository

import androidx.room.withTransaction
import com.example.fitnesapp.data.local.AppDatabase
import com.example.fitnesapp.domain.repository.MaintenanceRepository

class MaintenanceRepositoryImpl(
    private val database: AppDatabase
) : MaintenanceRepository {
    override suspend fun clearProgressData() {
        database.withTransaction {
            database.insightsDao().clearRecommendations()
            database.insightsDao().clearRecords()
            database.bodyMeasurementDao().clear()
        }
    }

    override suspend fun clearWorkoutHistory() {
        database.withTransaction {
            database.insightsDao().clearRecommendations()
            database.insightsDao().clearRecords()
            database.sessionDao().clearSets()
            database.sessionDao().clearSessionExercises()
            database.sessionDao().clearSessions()
        }
    }
}
