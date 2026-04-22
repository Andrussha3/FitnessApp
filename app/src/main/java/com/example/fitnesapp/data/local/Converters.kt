package com.example.fitnesapp.data.local

import androidx.room.TypeConverter
import com.example.fitnesapp.domain.model.ExerciseType
import com.example.fitnesapp.domain.model.Gender
import com.example.fitnesapp.domain.model.LoadUnit
import com.example.fitnesapp.domain.model.MuscleGroup
import com.example.fitnesapp.domain.model.PersonalRecordType
import com.example.fitnesapp.domain.model.RecommendationAction
import com.example.fitnesapp.domain.model.UserGoal
import com.example.fitnesapp.domain.model.WorkoutStage

class Converters {
    @TypeConverter
    fun fromGender(value: Gender?): String? = value?.name

    @TypeConverter
    fun toGender(value: String?): Gender? = value?.let(Gender::valueOf)

    @TypeConverter
    fun fromGoal(value: UserGoal?): String? = value?.name

    @TypeConverter
    fun toGoal(value: String?): UserGoal? = value?.let(UserGoal::valueOf)

    @TypeConverter
    fun fromExerciseType(value: ExerciseType?): String? = value?.name

    @TypeConverter
    fun toExerciseType(value: String?): ExerciseType? = value?.let(ExerciseType::valueOf)

    @TypeConverter
    fun fromLoadUnit(value: LoadUnit?): String? = value?.name

    @TypeConverter
    fun toLoadUnit(value: String?): LoadUnit? = value?.let(LoadUnit::valueOf)

    @TypeConverter
    fun fromMuscleGroup(value: MuscleGroup?): String? = value?.name

    @TypeConverter
    fun toMuscleGroup(value: String?): MuscleGroup? = value?.let(MuscleGroup::valueOf)

    @TypeConverter
    fun fromWorkoutStage(value: WorkoutStage?): String? = value?.name

    @TypeConverter
    fun toWorkoutStage(value: String?): WorkoutStage? = value?.let(WorkoutStage::valueOf)

    @TypeConverter
    fun fromRecommendationAction(value: RecommendationAction?): String? = value?.name

    @TypeConverter
    fun toRecommendationAction(value: String?): RecommendationAction? = value?.let(RecommendationAction::valueOf)

    @TypeConverter
    fun fromRecordType(value: PersonalRecordType?): String? = value?.name

    @TypeConverter
    fun toRecordType(value: String?): PersonalRecordType? = value?.let(PersonalRecordType::valueOf)
}
