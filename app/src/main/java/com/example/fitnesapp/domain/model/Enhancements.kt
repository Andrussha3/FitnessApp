package com.example.fitnesapp.domain.model

enum class MuscleGroup {
    CHEST, BACK, LEGS, SHOULDERS, BICEPS, TRICEPS, ABS, GLUTES, CALVES, FOREARMS, FULL_BODY, CARDIO, OTHER
}

enum class UserGoal {
    GAIN_MASS, LOSE_WEIGHT, INCREASE_STRENGTH, IMPROVE_ENDURANCE, MAINTAIN, OTHER
}

fun MuscleGroup.label(): String = when (this) {
    MuscleGroup.CHEST -> "Грудь"
    MuscleGroup.BACK -> "Спина"
    MuscleGroup.LEGS -> "Ноги"
    MuscleGroup.SHOULDERS -> "Плечи"
    MuscleGroup.BICEPS -> "Бицепс"
    MuscleGroup.TRICEPS -> "Трицепс"
    MuscleGroup.ABS -> "Пресс"
    MuscleGroup.GLUTES -> "Ягодицы"
    MuscleGroup.CALVES -> "Икры"
    MuscleGroup.FOREARMS -> "Предплечья"
    MuscleGroup.FULL_BODY -> "Все тело"
    MuscleGroup.CARDIO -> "Кардио / общее"
    MuscleGroup.OTHER -> "Другое"
}

fun UserGoal.label(): String = when (this) {
    UserGoal.GAIN_MASS -> "Набрать массу"
    UserGoal.LOSE_WEIGHT -> "Похудеть"
    UserGoal.INCREASE_STRENGTH -> "Увеличить силу"
    UserGoal.IMPROVE_ENDURANCE -> "Улучшить выносливость"
    UserGoal.MAINTAIN -> "Поддерживать форму"
    UserGoal.OTHER -> "Другая цель"
}

data class BodyMeasurement(
    val id: Long = 0L,
    val measuredAt: Long,
    val bodyWeightKg: Double? = null,
    val neckCm: Double? = null,
    val shouldersCm: Double? = null,
    val chestCm: Double? = null,
    val waistCm: Double? = null,
    val bellyCm: Double? = null,
    val hipsCm: Double? = null,
    val glutesCm: Double? = null,
    val bicepsCm: Double? = null,
    val forearmCm: Double? = null,
    val thighCm: Double? = null,
    val calfCm: Double? = null,
    val note: String = ""
)

data class MuscleGroupStats(
    val group: MuscleGroup,
    val exerciseCount: Int,
    val completedSets: Int,
    val totalVolume: Double,
    val sessionsCount: Int,
    val sharePercent: Double,
    val isLagging: Boolean
)

data class GoalFocusedMetrics(
    val title: String,
    val subtitle: String,
    val primaryValue: String,
    val secondaryValue: String
)

data class ProgramTemplate(
    val id: String,
    val name: String,
    val description: String,
    val isRecommended: Boolean = false,
    val days: List<WorkoutPlanDay>
)
