package com.example.fitnesapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.fitnesapp.data.local.entity.WorkoutPlanDayEntity
import com.example.fitnesapp.data.local.entity.WorkoutPlanDayWithExercises
import com.example.fitnesapp.data.local.entity.WorkoutPlanExerciseEntity
import com.example.fitnesapp.data.local.entity.WorkoutPlanExerciseResolved
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutPlanDao {
    @Transaction
    @Query("SELECT * FROM workout_plan_days ORDER BY dayOfWeek ASC")
    fun observePlanDays(): Flow<List<WorkoutPlanDayWithExercises>>

    @Query(
        """
        SELECT wpe.id, wpe.dayOfWeek, wpe.exerciseId, wpe.orderInDay, wpe.targetSets, wpe.targetReps,
        wpe.targetWeight, wpe.restSeconds, wpe.note, e.name AS exerciseName, e.muscleGroupTag AS muscleGroupTag, e.type AS exerciseType,
        e.loadUnit AS loadUnit
        FROM workout_plan_exercises wpe
        INNER JOIN exercises e ON e.id = wpe.exerciseId
        ORDER BY wpe.dayOfWeek ASC, wpe.orderInDay ASC
        """
    )
    fun observeResolvedExercises(): Flow<List<WorkoutPlanExerciseResolved>>

    @Transaction
    @Query("SELECT * FROM workout_plan_days WHERE dayOfWeek = :dayOfWeek LIMIT 1")
    suspend fun getDay(dayOfWeek: Int): WorkoutPlanDayWithExercises?

    @Query(
        """
        SELECT wpe.id, wpe.dayOfWeek, wpe.exerciseId, wpe.orderInDay, wpe.targetSets, wpe.targetReps,
        wpe.targetWeight, wpe.restSeconds, wpe.note, e.name AS exerciseName, e.muscleGroupTag AS muscleGroupTag, e.type AS exerciseType,
        e.loadUnit AS loadUnit
        FROM workout_plan_exercises wpe
        INNER JOIN exercises e ON e.id = wpe.exerciseId
        WHERE wpe.dayOfWeek = :dayOfWeek
        ORDER BY wpe.orderInDay ASC
        """
    )
    suspend fun getResolvedExercisesForDay(dayOfWeek: Int): List<WorkoutPlanExerciseResolved>

    @Query("SELECT * FROM workout_plan_exercises WHERE dayOfWeek = :dayOfWeek ORDER BY orderInDay ASC")
    suspend fun getExercisesForDay(dayOfWeek: Int): List<WorkoutPlanExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDay(day: WorkoutPlanDayEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<WorkoutPlanExerciseEntity>)

    @Query("DELETE FROM workout_plan_exercises WHERE dayOfWeek = :dayOfWeek")
    suspend fun deleteExercisesForDay(dayOfWeek: Int)

    @Query("DELETE FROM workout_plan_exercises")
    suspend fun clearExercises()

    @Query("DELETE FROM workout_plan_days")
    suspend fun clearDays()
}
