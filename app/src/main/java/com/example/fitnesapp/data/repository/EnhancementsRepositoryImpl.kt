package com.example.fitnesapp.data.repository

import com.example.fitnesapp.data.toDomain
import com.example.fitnesapp.data.toEntity
import com.example.fitnesapp.domain.model.BodyMeasurement
import com.example.fitnesapp.domain.model.ExerciseType
import com.example.fitnesapp.domain.model.GoalFocusedMetrics
import com.example.fitnesapp.domain.model.MuscleGroup
import com.example.fitnesapp.domain.model.MuscleGroupStats
import com.example.fitnesapp.domain.model.ProgramTemplate
import com.example.fitnesapp.domain.model.UserGoal
import com.example.fitnesapp.domain.model.WorkoutPlanDay
import com.example.fitnesapp.domain.model.WorkoutPlanExercise
import com.example.fitnesapp.domain.model.completedSetsCount
import com.example.fitnesapp.domain.model.dayLabel
import com.example.fitnesapp.domain.model.label
import com.example.fitnesapp.domain.model.strengthVolume
import com.example.fitnesapp.domain.model.toLocalDate
import com.example.fitnesapp.domain.repository.AnalyticsRepository
import com.example.fitnesapp.domain.repository.BodyMeasurementRepository
import com.example.fitnesapp.domain.repository.ProfileRepository
import com.example.fitnesapp.domain.repository.ProgramTemplateRepository
import com.example.fitnesapp.domain.repository.SessionRepository
import com.example.fitnesapp.data.local.dao.BodyMeasurementDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class BodyMeasurementRepositoryImpl(
    private val dao: BodyMeasurementDao
) : BodyMeasurementRepository {
    override fun observeHistory(): Flow<List<BodyMeasurement>> = dao.observeAll().map { it.map { e -> e.toDomain() } }
    override fun observeLatest(): Flow<BodyMeasurement?> = dao.observeLatest().map { it?.toDomain() }
    override suspend fun save(measurement: BodyMeasurement) = dao.upsert(measurement.toEntity())
    override suspend fun delete(id: Long) = dao.delete(id)
}

class ProgramTemplateRepositoryImpl : ProgramTemplateRepository {
    override fun getTemplates(): List<ProgramTemplate> = listOf(
        ProgramTemplate("lupolenochka", "Тренировка Луполеночки", "Вторник: грудь/плечи/трицепс, четверг: спина/бицепс/пресс, воскресенье: ноги/икры", true, listOf(
            WorkoutPlanDay(1, true, emptyList(), 1.dayLabel()),
            templateDay(2, "Тренировка Луполеночки - грудь + плечи + трицепс", false, listOf(
                templateExercise("Жим штанги лежа", 4, 8, 50.0, 120),
                templateExercise("Жим от груди в тренажере", 3, 10, 35.0, 90),
                templateExercise("Жим гантелей сидя на плечи", 3, 10, 14.0, 90),
                templateExercise("Разведения гантелей в стороны", 3, 15, 8.0, 75),
                templateExercise("Разгибание рук на верхнем блоке", 3, 12, 20.0, 75),
                templateExercise("Бабочка / сведение рук в тренажере", 3, 12, 25.0, 75),
                templateExercise("Легкое добивание бицепса", 2, 12, 10.0, 60)
            )),
            WorkoutPlanDay(3, true, emptyList(), 3.dayLabel()),
            templateDay(4, "Тренировка Луполеночки - спина + бицепс + пресс", false, listOf(
                templateExercise("Тяга вертикального блока к груди", 4, 10, 40.0, 90),
                templateExercise("Тяга горизонтального блока к поясу", 4, 10, 42.5, 90),
                templateExercise("Тяга в тренажере с упором в грудь", 3, 10, 35.0, 90),
                templateExercise("Сгибание рук со штангой / EZ-грифом", 3, 10, 20.0, 75),
                templateExercise("Сгибание рук в тренажере / на скамье Скотта", 3, 12, 17.5, 75),
                templateExercise("Пресс - упражнение 1", 3, 15, null, 60),
                templateExercise("Пресс - упражнение 2", 3, 15, null, 60),
                templateExercise("Face pull или задняя дельта в тренажере", 3, 15, 15.0, 60)
            )),
            WorkoutPlanDay(5, true, emptyList(), 5.dayLabel()),
            WorkoutPlanDay(6, true, emptyList(), 6.dayLabel()),
            templateDay(7, "Тренировка Луполеночки - ноги + икры", false, listOf(
                templateExercise("Приседания со штангой или в Смите", 4, 8, 60.0, 120),
                templateExercise("Жим ногами", 3, 12, 110.0, 90),
                templateExercise("Сгибание ног лежа", 3, 12, 30.0, 75),
                templateExercise("Разгибание ног в тренажере", 3, 12, 35.0, 75),
                templateExercise("Подъем на носки стоя или сидя", 4, 15, 40.0, 60),
                templateExercise("Кардио по самочувствию", 1, 15, null, 0)
            ))
        )),
        ProgramTemplate("fullbody3", "Фулбоди 3 раза в неделю", "Базовый шаблон на все тело", false, listOf(
            templateDay(1, "Фулбоди A", false, listOf(templateExercise("Приседания", 4, 8, 60.0), templateExercise("Жим лежа", 4, 8, 50.0), templateExercise("Тяга верхнего блока", 3, 10, 35.0))),
            WorkoutPlanDay(2, true, emptyList(), 2.dayLabel()),
            templateDay(3, "Фулбоди B", false, listOf(templateExercise("Жим ногами", 4, 10, 100.0), templateExercise("Жим гантелей сидя на плечи", 3, 10, 12.0), templateExercise("Тяга горизонтального блока к поясу", 3, 10, 35.0))),
            WorkoutPlanDay(4, true, emptyList(), 4.dayLabel()),
            templateDay(5, "Фулбоди C", false, listOf(templateExercise("Румынская тяга", 4, 8, 60.0), templateExercise("Жим от груди в тренажере", 3, 10, 35.0), templateExercise("Face pull или задняя дельта в тренажере", 3, 15, 12.5))),
            WorkoutPlanDay(6, true, emptyList(), 6.dayLabel()),
            WorkoutPlanDay(7, true, emptyList(), 7.dayLabel())
        )),
        ProgramTemplate("upperlower", "Верх / Низ", "Разделение верха и низа тела", false, listOf(
            templateDay(1, "Верх", false, listOf(templateExercise("Жим лежа", 4, 8, 50.0), templateExercise("Тяга верхнего блока", 4, 10, 35.0), templateExercise("Сгибание рук со штангой / EZ-грифом", 3, 10, 20.0))),
            templateDay(2, "Низ", false, listOf(templateExercise("Приседания", 4, 8, 60.0), templateExercise("Жим ногами", 3, 12, 100.0), templateExercise("Подъем на носки стоя или сидя", 4, 15, 30.0))),
            WorkoutPlanDay(3, true, emptyList(), 3.dayLabel()),
            templateDay(4, "Верх", false, listOf(templateExercise("Жим гантелей сидя на плечи", 3, 10, 12.0), templateExercise("Тяга горизонтального блока к поясу", 4, 10, 35.0), templateExercise("Разгибание рук на верхнем блоке", 3, 12, 20.0))),
            templateDay(5, "Низ", false, listOf(templateExercise("Румынская тяга", 4, 8, 60.0), templateExercise("Сгибание ног лежа", 3, 12, 25.0), templateExercise("Пресс - упражнение 1", 3, 15, null))),
            WorkoutPlanDay(6, true, emptyList(), 6.dayLabel()), WorkoutPlanDay(7, true, emptyList(), 7.dayLabel())
        )),
        ProgramTemplate("ppl", "Push Pull Legs", "Классический сплит push/pull/legs", false, listOf(
            templateDay(1, "Push", false, listOf(templateExercise("Жим лежа", 4, 8, 50.0), templateExercise("Жим гантелей сидя на плечи", 3, 10, 12.0), templateExercise("Разгибание рук на верхнем блоке", 3, 12, 20.0))),
            templateDay(2, "Pull", false, listOf(templateExercise("Тяга верхнего блока", 4, 10, 35.0), templateExercise("Тяга горизонтального блока к поясу", 3, 10, 35.0), templateExercise("Сгибание рук со штангой / EZ-грифом", 3, 10, 20.0))),
            templateDay(3, "Legs", false, listOf(templateExercise("Приседания", 4, 8, 60.0), templateExercise("Жим ногами", 3, 12, 100.0), templateExercise("Подъем на носки стоя или сидя", 4, 15, 30.0))),
            WorkoutPlanDay(4, true, emptyList(), 4.dayLabel()), WorkoutPlanDay(5, true, emptyList(), 5.dayLabel()), WorkoutPlanDay(6, true, emptyList(), 6.dayLabel()), WorkoutPlanDay(7, true, emptyList(), 7.dayLabel())
        )),
        ProgramTemplate("strength", "Базовая силовая программа", "Акцент на базовые движения", false, listOf(
            templateDay(1, "Сила A", false, listOf(templateExercise("Приседания", 5, 5, 70.0), templateExercise("Жим лежа", 5, 5, 55.0), templateExercise("Тяга горизонтального блока к поясу", 4, 8, 40.0))),
            WorkoutPlanDay(2, true, emptyList(), 2.dayLabel()),
            templateDay(3, "Сила B", false, listOf(templateExercise("Румынская тяга", 5, 5, 70.0), templateExercise("Жим гантелей сидя на плечи", 4, 6, 14.0), templateExercise("Тяга верхнего блока", 4, 8, 40.0))),
            WorkoutPlanDay(4, true, emptyList(), 4.dayLabel()),
            templateDay(5, "Сила C", false, listOf(templateExercise("Жим ногами", 5, 8, 120.0), templateExercise("Жим от груди в тренажере", 4, 8, 40.0), templateExercise("Face pull или задняя дельта в тренажере", 3, 15, 12.5))),
            WorkoutPlanDay(6, true, emptyList(), 6.dayLabel()), WorkoutPlanDay(7, true, emptyList(), 7.dayLabel())
        )),
        ProgramTemplate("bro_split", "Bro Split 5 дней", "Популярный сплит по одной-двум мышечным группам в день", false, listOf(
            templateDay(1, "Грудь", false, listOf(templateExercise("Жим лежа", 4, 8, 50.0), templateExercise("Жим от груди в тренажере", 3, 10, 35.0), templateExercise("Бабочка / сведение рук в тренажере", 3, 12, 25.0))),
            templateDay(2, "Спина", false, listOf(templateExercise("Тяга верхнего блока", 4, 10, 35.0), templateExercise("Тяга горизонтального блока к поясу", 4, 10, 35.0), templateExercise("Face pull или задняя дельта в тренажере", 3, 15, 12.5))),
            templateDay(3, "Плечи", false, listOf(templateExercise("Жим гантелей сидя на плечи", 4, 8, 14.0), templateExercise("Разведения гантелей в стороны", 4, 15, 8.0), templateExercise("Face pull или задняя дельта в тренажере", 3, 15, 12.5))),
            templateDay(4, "Ноги", false, listOf(templateExercise("Приседания со штангой или в Смите", 4, 8, 60.0), templateExercise("Жим ногами", 3, 12, 100.0), templateExercise("Сгибание ног лежа", 3, 12, 25.0))),
            templateDay(5, "Руки", false, listOf(templateExercise("Сгибание рук со штангой / EZ-грифом", 3, 10, 20.0), templateExercise("Сгибание рук в тренажере / на скамье Скотта", 3, 12, 17.5), templateExercise("Разгибание рук на верхнем блоке", 3, 12, 20.0))),
            WorkoutPlanDay(6, true, emptyList(), 6.dayLabel()), WorkoutPlanDay(7, true, emptyList(), 7.dayLabel())
        )),
        ProgramTemplate("arnold_split", "Arnold Split", "Грудь/спина, плечи/руки, ноги - два круга в неделю", false, listOf(
            templateDay(1, "Грудь + Спина", false, listOf(templateExercise("Жим лежа", 4, 8, 50.0), templateExercise("Жим от груди в тренажере", 3, 10, 35.0), templateExercise("Тяга верхнего блока", 4, 10, 35.0), templateExercise("Тяга горизонтального блока к поясу", 3, 10, 35.0))),
            templateDay(2, "Плечи + Руки", false, listOf(templateExercise("Жим гантелей сидя на плечи", 4, 8, 14.0), templateExercise("Разведения гантелей в стороны", 3, 15, 8.0), templateExercise("Сгибание рук со штангой / EZ-грифом", 3, 10, 20.0), templateExercise("Разгибание рук на верхнем блоке", 3, 12, 20.0))),
            templateDay(3, "Ноги", false, listOf(templateExercise("Приседания со штангой или в Смите", 4, 8, 60.0), templateExercise("Жим ногами", 4, 10, 100.0), templateExercise("Подъем на носки стоя или сидя", 4, 15, 30.0))),
            templateDay(4, "Грудь + Спина", false, listOf(templateExercise("Жим лежа", 3, 10, 47.5), templateExercise("Бабочка / сведение рук в тренажере", 3, 12, 25.0), templateExercise("Тяга верхнего блока", 3, 12, 32.5), templateExercise("Face pull или задняя дельта в тренажере", 3, 15, 12.5))),
            templateDay(5, "Плечи + Руки", false, listOf(templateExercise("Жим гантелей сидя на плечи", 3, 10, 12.0), templateExercise("Разведения гантелей в стороны", 3, 15, 7.0), templateExercise("Сгибание рук в тренажере / на скамье Скотта", 3, 12, 17.5), templateExercise("Разгибание рук на верхнем блоке", 3, 12, 20.0))),
            templateDay(6, "Ноги", false, listOf(templateExercise("Жим ногами", 4, 12, 100.0), templateExercise("Сгибание ног лежа", 3, 12, 25.0), templateExercise("Разгибание ног в тренажере", 3, 12, 30.0))),
            WorkoutPlanDay(7, true, emptyList(), 7.dayLabel())
        )),
        ProgramTemplate("hypertrophy4", "Гипертрофия 4 дня", "Популярный четырехдневный сплит на массу", false, listOf(
            templateDay(1, "Верх A", false, listOf(templateExercise("Жим лежа", 4, 8, 50.0), templateExercise("Тяга верхнего блока", 4, 10, 35.0), templateExercise("Жим гантелей сидя на плечи", 3, 10, 12.0))),
            templateDay(2, "Низ A", false, listOf(templateExercise("Приседания со штангой или в Смите", 4, 8, 60.0), templateExercise("Сгибание ног лежа", 3, 12, 25.0), templateExercise("Подъем на носки стоя или сидя", 4, 15, 30.0))),
            WorkoutPlanDay(3, true, emptyList(), 3.dayLabel()),
            templateDay(4, "Верх B", false, listOf(templateExercise("Жим от груди в тренажере", 4, 10, 35.0), templateExercise("Тяга горизонтального блока к поясу", 4, 10, 35.0), templateExercise("Разгибание рук на верхнем блоке", 3, 12, 20.0), templateExercise("Сгибание рук со штангой / EZ-грифом", 3, 10, 20.0))),
            templateDay(5, "Низ B", false, listOf(templateExercise("Жим ногами", 4, 12, 100.0), templateExercise("Разгибание ног в тренажере", 3, 12, 30.0), templateExercise("Сгибание ног лежа", 3, 12, 25.0))),
            WorkoutPlanDay(6, true, emptyList(), 6.dayLabel()), WorkoutPlanDay(7, true, emptyList(), 7.dayLabel())
        )),
        ProgramTemplate("beginner2", "Новичок 2 дня", "Минималистичная программа для старта", false, listOf(
            templateDay(1, "День A", false, listOf(templateExercise("Приседания", 3, 8, 40.0), templateExercise("Жим лежа", 3, 8, 35.0), templateExercise("Тяга верхнего блока", 3, 10, 25.0), templateExercise("Пресс - упражнение 1", 3, 15, null))),
            WorkoutPlanDay(2, true, emptyList(), 2.dayLabel()),
            templateDay(3, "День B", false, listOf(templateExercise("Жим ногами", 3, 10, 80.0), templateExercise("Жим гантелей сидя на плечи", 3, 10, 10.0), templateExercise("Тяга горизонтального блока к поясу", 3, 10, 30.0), templateExercise("Подъем на носки стоя или сидя", 3, 15, 20.0))),
            WorkoutPlanDay(4, true, emptyList(), 4.dayLabel()), WorkoutPlanDay(5, true, emptyList(), 5.dayLabel()), WorkoutPlanDay(6, true, emptyList(), 6.dayLabel()), WorkoutPlanDay(7, true, emptyList(), 7.dayLabel())
        )),
        ProgramTemplate("empty", "Пустой шаблон", "Неделя без заполнения, ручная настройка", false, (1..7).map { WorkoutPlanDay(it, true, emptyList(), it.dayLabel()) })
    )
}

class AnalyticsRepositoryImpl(
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository,
    private val bodyMeasurementRepository: BodyMeasurementRepository
) : AnalyticsRepository {
    override fun observeMuscleGroupStats(periodDays: Int?): Flow<List<MuscleGroupStats>> = sessionRepository.observeHistory().map { sessions ->
        val filtered = sessions.filter { periodDays == null || it.startedAt.toLocalDate().isAfter(LocalDate.now().minusDays(periodDays.toLong()).minusDays(1)) }
        val totalVolume = filtered.sumOf { it.strengthVolume() }.takeIf { it > 0.0 } ?: 1.0
        MuscleGroup.entries.map { group ->
            val exercises = filtered.flatMap { it.exercises }.filter { it.muscleGroupTag == group }
            val doneSets = exercises.flatMap { it.sets }.filter { it.completed && !it.skipped }
            val volume = doneSets.sumOf { (it.actualWeight ?: 0.0) * (it.actualReps ?: 0) }
            MuscleGroupStats(group, exercises.size, doneSets.size, volume, filtered.count { s -> s.exercises.any { it.muscleGroupTag == group } }, (volume / totalVolume) * 100.0, doneSets.size < 4)
        }.filter { it.exerciseCount > 0 || it.completedSets > 0 }
    }

    override fun observeGoalFocusedMetrics(periodDays: Int?): Flow<List<GoalFocusedMetrics>> = combine(
        sessionRepository.observeHistory(),
        profileRepository.observeProfile(),
        bodyMeasurementRepository.observeLatest()
    ) { sessions, profile, latestMeasurement ->
        val filtered = sessions.filter { periodDays == null || it.startedAt.toLocalDate().isAfter(LocalDate.now().minusDays(periodDays.toLong()).minusDays(1)) }
        when (profile?.goal ?: UserGoal.MAINTAIN) {
            UserGoal.GAIN_MASS -> listOf(GoalFocusedMetrics("Набор массы", "Рост объема и массы тела", filtered.sumOf { it.strengthVolume() }.toInt().toString(), (latestMeasurement?.bodyWeightKg ?: profile?.weightKg ?: 0.0).toString()))
            UserGoal.LOSE_WEIGHT -> listOf(GoalFocusedMetrics("Похудение", "Вес тела и частота тренировок", (latestMeasurement?.bodyWeightKg ?: profile?.weightKg ?: 0.0).toString(), filtered.size.toString()))
            UserGoal.INCREASE_STRENGTH -> listOf(GoalFocusedMetrics("Сила", "Объем и силовые сессии", filtered.maxOfOrNull { it.strengthVolume() }?.toInt().toString(), filtered.size.toString()))
            UserGoal.IMPROVE_ENDURANCE -> listOf(GoalFocusedMetrics("Выносливость", "Кардио и регулярность", filtered.flatMap { it.exercises }.count { it.exerciseType == ExerciseType.CARDIO }.toString(), filtered.size.toString()))
            UserGoal.MAINTAIN, UserGoal.OTHER -> listOf(GoalFocusedMetrics("Поддержание формы", "Стабильность и общий объем", filtered.size.toString(), filtered.sumOf { it.completedSetsCount() }.toString()))
        }
    }

    override suspend fun updateUserGoal(goal: UserGoal, note: String) {
        // Goal is persisted through UserProfile save flow on Settings screen.
    }
}

private fun templateDay(dayOfWeek: Int, title: String, isRest: Boolean, exercises: List<WorkoutPlanExercise>) = WorkoutPlanDay(dayOfWeek, isRest, exercises, title)

private fun templateExercise(name: String, sets: Int, reps: Int, weight: Double?, rest: Int = 120) = WorkoutPlanExercise(
    dayOfWeek = 1,
    exerciseId = 0L,
    exerciseName = name,
    muscleGroupTag = when {
        name.contains("жим", true) && name.contains("ног", true) -> MuscleGroup.LEGS
        name.contains("жим", true) -> MuscleGroup.CHEST
        name.contains("тяга", true) -> MuscleGroup.BACK
        name.contains("присед", true) -> MuscleGroup.LEGS
        name.contains("икр", true) || name.contains("носк", true) -> MuscleGroup.CALVES
        name.contains("бицеп", true) || name.contains("сгибание рук", true) -> MuscleGroup.BICEPS
        name.contains("трицеп", true) || name.contains("разгибание рук", true) -> MuscleGroup.TRICEPS
        name.contains("пресс", true) -> MuscleGroup.ABS
        else -> MuscleGroup.OTHER
    },
    exerciseType = if (weight == null) ExerciseType.CARDIO else ExerciseType.STRENGTH,
    loadUnit = if (weight == null) com.example.fitnesapp.domain.model.LoadUnit.REPS else com.example.fitnesapp.domain.model.LoadUnit.KG,
    orderInDay = 1,
    targetSets = sets,
    targetReps = reps,
    targetWeight = weight,
    restSeconds = rest,
    note = ""
)
