# ЧекТрекер

Мобильное приложение для учёта личных финансов по чекам: добавляйте расходы,
смотрите аналитику за неделю и месяц, пересчёт в USD по актуальным курсам.

## Технологии

- Kotlin + Jetpack Compose (Material Design 3, светлая и тёмная темы)
- MVVM + Clean Architecture (data / domain / presentation)
- Room — локальное хранение, без регистрации
- Retrofit + Kotlinx Serialization — курсы валют с open.er-api.com
- Coroutines + Flow
- Koin — внедрение зависимостей

## Сборка

1. Откройте проект в Android Studio (Koala или новее).
2. Дождитесь синхронизации Gradle.
3. Запустите конфигурацию `app` на эмуляторе или устройстве (Android 8.0+).
