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

## Release подпись

Проект поддерживает release-подпись двумя способами:

- локально через `keystore.properties`
- в CI через GitHub Secrets

Локальный шаблон лежит в `keystore.properties.example`.

Нужные параметры:

- `MYAPP_UPLOAD_STORE_FILE`
- `MYAPP_UPLOAD_KEY_ALIAS`
- `MYAPP_UPLOAD_STORE_PASSWORD`
- `MYAPP_UPLOAD_KEY_PASSWORD`

Если release-ключ недоступен, локальная release-сборка автоматически fallback-ится на debug-подпись.

## GitHub Actions Release

Добавлен workflow `.github/workflows/android-release.yml`.

Он умеет:

- собирать release APK вручную через `workflow_dispatch`
- собирать и публиковать APK при push тега вида `v*`
- прикладывать APK как artifact
- публиковать APK в GitHub Release для tag-сборок

Нужно добавить в GitHub Secrets:

- `MYAPP_UPLOAD_KEYSTORE_BASE64`
- `MYAPP_UPLOAD_KEY_ALIAS`
- `MYAPP_UPLOAD_STORE_PASSWORD`
- `MYAPP_UPLOAD_KEY_PASSWORD`

Для `MYAPP_UPLOAD_KEYSTORE_BASE64` нужно закодировать keystore в base64.

## Примечания

- Приложение работает без сервера и без интернета.
- Все данные хранятся локально через Room.
- В проекте нет обязательных тестов по текущей договоренности.
