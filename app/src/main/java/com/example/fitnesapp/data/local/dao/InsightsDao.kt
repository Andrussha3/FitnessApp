package com.example.fitnesapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitnesapp.data.local.entity.PersonalRecordEntity
import com.example.fitnesapp.data.local.entity.ProgressionRecommendationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InsightsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendations(items: List<ProgressionRecommendationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(items: List<PersonalRecordEntity>)

    @Query("SELECT * FROM progression_recommendations WHERE sessionId = :sessionId ORDER BY id ASC")
    fun observeRecommendationsForSession(sessionId: Long): Flow<List<ProgressionRecommendationEntity>>

    @Query("SELECT * FROM progression_recommendations WHERE sessionId = :sessionId ORDER BY id ASC")
    suspend fun getRecommendationsForSession(sessionId: Long): List<ProgressionRecommendationEntity>

    @Query("SELECT * FROM progression_recommendations WHERE exerciseId = :exerciseId ORDER BY id DESC LIMIT 1")
    suspend fun getLatestRecommendationForExercise(exerciseId: Long): ProgressionRecommendationEntity?

    @Query("SELECT * FROM personal_records WHERE sessionId = :sessionId ORDER BY achievedAt DESC")
    fun observeRecordsForSession(sessionId: Long): Flow<List<PersonalRecordEntity>>

    @Query("SELECT * FROM personal_records WHERE sessionId = :sessionId ORDER BY achievedAt DESC")
    suspend fun getRecordsForSession(sessionId: Long): List<PersonalRecordEntity>

    @Query("SELECT * FROM personal_records WHERE exerciseId = :exerciseId AND recordType = :recordType ORDER BY value DESC LIMIT 1")
    suspend fun getTopExerciseRecord(exerciseId: Long, recordType: com.example.fitnesapp.domain.model.PersonalRecordType): PersonalRecordEntity?

    @Query("SELECT * FROM personal_records WHERE recordType = 'MAX_WORKOUT_VOLUME' ORDER BY value DESC LIMIT 1")
    suspend fun getTopWorkoutVolumeRecord(): PersonalRecordEntity?

    @Query("SELECT * FROM personal_records ORDER BY achievedAt DESC LIMIT :limit")
    fun observeLatestRecords(limit: Int): Flow<List<PersonalRecordEntity>>

    @Query("DELETE FROM progression_recommendations")
    suspend fun clearRecommendations()

    @Query("DELETE FROM personal_records")
    suspend fun clearRecords()
}
