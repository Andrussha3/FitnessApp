# Gym Log

Нативное Android-приложение на Kotlin для полностью офлайн-учета тренировок в спортзале.

## Стек

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Room
- ViewModel
- Coroutines
- Flow / StateFlow
- MVVM + Repository + UseCase

## Архитектура

Проект разделен на 3 слоя:

- `data`: Room entities, DAO, база данных, repository implementation, локальные уведомления, demo seed
- `domain`: модели, репозитории, use case, расчеты прогресса
- `presentation`: Compose UI, ViewModel, экраны, ui state, навигация

## Основные возможности

- локальный профиль пользователя
- создание, редактирование и удаление упражнений
- недельный план тренировок на 7 дней
- запуск тренировки на текущий день
- сохранение незавершенной тренировки
- отметка фактических подходов, повторений и весов
- таймер отдыха с локальным уведомлением
- история завершенных тренировок
- экран прогресса с локальными графиками
- заметки по тренировкам и самочувствию
- демо-данные при первом запуске

## Структура проекта

```text
app/
  src/main/
    java/com/example/fitnesapp/
      data/
        local/
        notification/
        repository/
      domain/
        model/
        repository/
        usecase/
      presentation/
        component/
        navigation/
        screen/
        theme/
      FitnesApplication.kt
      MainActivity.kt
    res/
```

## Запуск

1. Откройте проект в Android Studio.
2. Дождитесь синхронизации Gradle.
3. Убедитесь, что используется встроенный JBR Android Studio или JDK 17+.
4. Запустите приложение на эмуляторе или устройстве Android.

## Сборка из терминала

```bash
./gradlew.bat assembleDebug
```

## Примечания

- Приложение работает без сервера и без интернета.
- Все данные хранятся локально через Room.
- В проекте нет обязательных тестов по текущей договоренности.
