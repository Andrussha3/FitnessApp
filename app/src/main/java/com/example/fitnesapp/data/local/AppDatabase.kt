package com.example.fitnesapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fitnesapp.data.local.dao.ExerciseDao
import com.example.fitnesapp.data.local.dao.BodyMeasurementDao
import com.example.fitnesapp.data.local.dao.InsightsDao
import com.example.fitnesapp.data.local.dao.NoteDao
import com.example.fitnesapp.data.local.dao.ProfileDao
import com.example.fitnesapp.data.local.dao.SessionDao
import com.example.fitnesapp.data.local.dao.WorkoutPlanDao
import com.example.fitnesapp.data.local.entity.BodyMeasurementEntity
import com.example.fitnesapp.data.local.entity.ExerciseEntity
import com.example.fitnesapp.data.local.entity.NoteEntity
import com.example.fitnesapp.data.local.entity.PersonalRecordEntity
import com.example.fitnesapp.data.local.entity.ProfileEntity
import com.example.fitnesapp.data.local.entity.ProgressionRecommendationEntity
import com.example.fitnesapp.data.local.entity.SessionExerciseEntity
import com.example.fitnesapp.data.local.entity.WorkoutPlanDayEntity
import com.example.fitnesapp.data.local.entity.WorkoutPlanExerciseEntity
import com.example.fitnesapp.data.local.entity.WorkoutSessionEntity
import com.example.fitnesapp.data.local.entity.WorkoutSetEntity

@Database(
    entities = [
        ProfileEntity::class,
        BodyMeasurementEntity::class,
        ExerciseEntity::class,
        WorkoutPlanDayEntity::class,
        WorkoutPlanExerciseEntity::class,
        WorkoutSessionEntity::class,
        SessionExerciseEntity::class,
        WorkoutSetEntity::class,
        NoteEntity::class,
        ProgressionRecommendationEntity::class,
        PersonalRecordEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun sessionDao(): SessionDao
    abstract fun noteDao(): NoteDao
    abstract fun insightsDao(): InsightsDao
}
