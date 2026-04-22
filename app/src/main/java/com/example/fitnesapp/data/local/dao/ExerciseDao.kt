package com.example.fitnesapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitnesapp.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises WHERE (:includeArchived = 1 OR isArchived = 0) ORDER BY updatedAt DESC, name ASC")
    fun observeExercises(includeArchived: Boolean): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: Long): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE name = :name ORDER BY id ASC LIMIT 1")
    suspend fun getByName(name: String): ExerciseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ExerciseEntity): Long

    @Query("UPDATE exercises SET isArchived = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archive(id: Long, updatedAt: Long)

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM workout_plan_exercises WHERE exerciseId = :exerciseId")
    suspend fun countPlanUsage(exerciseId: Long): Int

    @Query("DELETE FROM exercises")
    suspend fun clear()
}
