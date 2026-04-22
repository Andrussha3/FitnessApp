package com.example.fitnesapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitnesapp.data.local.entity.BodyMeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMeasurementDao {
    @Query("SELECT * FROM body_measurements ORDER BY measuredAt DESC")
    fun observeAll(): Flow<List<BodyMeasurementEntity>>

    @Query("SELECT * FROM body_measurements ORDER BY measuredAt DESC LIMIT 1")
    fun observeLatest(): Flow<BodyMeasurementEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: BodyMeasurementEntity)

    @Query("DELETE FROM body_measurements WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM body_measurements")
    suspend fun clear()
}
