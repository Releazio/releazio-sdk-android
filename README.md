# Releazio Android SDK

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/Platform-Android%20API%2024%2B-green.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![JitPack](https://jitpack.io/v/Releazio/releazio-sdk-android.svg)](https://jitpack.io/#Releazio/releazio-sdk-android)

**Releazio Android SDK** — современная библиотека для управления обновлениями приложений в Android. SDK предоставляет полный набор инструментов для проверки обновлений, отображения changelog и управления различными типами обновлений.

## ✨ Основные возможности

- 🚀 **Проверка обновлений** — Автоматическая проверка наличия новых версий через API
- 🎯 **4 типа обновлений** — Поддержка latest, silent, popup и popup force режимов
- 📝 **Changelog** — Отображение изменений с поддержкой WebView для постов
- 🎨 **UI компоненты** — Готовые компоненты для Jetpack Compose и View System
- 🌍 **Локализация** — Поддержка английского и русского языков
- 🔔 **Бейджи и уведомления** — Индикаторы новых версий
- ⚙️ **Гибкая настройка** — Кастомизация цветов, локали и поведения
- 🔄 **Suspend функции** — Современный async/await подход с Kotlin Coroutines

## 📋 Требования

- Android API 24+ (Android 7.0 Nougat)
- Kotlin 2.0.21+
- Jetpack Compose (опционально, для UI компонентов)

## 📦 Установка

### Gradle

**Добавьте JitPack репозиторий в `settings.gradle.kts` (или `settings.gradle`):**

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

**Добавьте зависимость в `build.gradle.kts` (module):**

```kotlin
dependencies {
    implementation("com.github.Releazio:releazio-sdk-android:1.0.4")
}
```

**Или в `build.gradle` (module):**

```gradle
dependencies {
    implementation 'com.github.Releazio:releazio-sdk-android:1.0.4'
}
```

## 🚀 Быстрый старт

### 1. Импортируйте SDK

```kotlin
import com.releazio.sdk.Releazio
import com.releazio.sdk.core.ReleazioConfiguration
```

### 2. Настройте SDK в Application

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val configuration = ReleazioConfiguration(
            apiKey = "your-api-key",
            locale = "ru", // или "en"
            debugLoggingEnabled = true
        )
        
        Releazio.configure(configuration, this)
    }
}
```

### 3. Проверьте обновления

```kotlin
// В Activity или Fragment
lifecycleScope.launch {
    try {
        val updateState = Releazio.checkUpdates()
        
        // Проверьте нужно ли показывать попап
        if (updateState.shouldShowPopup) {
            // Покажите диалог обновления
        }
        
        // Проверьте нужно ли показывать бейдж
        if (updateState.shouldShowBadge) {
            // Покажите BadgeView
        }
        
        // Проверьте нужно ли показывать кнопку обновления
        if (updateState.shouldShowUpdateButton) {
            // Покажите кнопку обновления
        }
    } catch (e: ReleazioError) {
        Log.e("Releazio", "Ошибка проверки обновлений: ${e.message}")
    }
}
```

## 📚 Типы обновлений

SDK поддерживает 4 типа обновлений в соответствии с API:

- **Type 0 (latest)** — Показывается бейдж, при клике открывается post_url
- **Type 1 (silent)** — Только кнопка "Обновить", попап не показывается
- **Type 2 (popup)** — Закрываемый попап с поддержкой show_interval
- **Type 3 (popup force)** — Незакрываемый попап с ограниченным количеством пропусков (skip_attempts)

## 🎨 UI компоненты

### Jetpack Compose

#### ReleazioUpdateDialog
Диалог для обновлений с поддержкой двух стилей: Native Android Alert и Material3.

```kotlin
ReleazioUpdateDialog(
    updateState = updateState,
    style = DialogStyle.Native, // или .Material3
    onUpdate = {
        Releazio.openAppStore(updateState)
    },
    onSkip = { remaining ->
        Releazio.skipUpdate(updateState.currentVersion)
    },
    onDismiss = {
        // Закрыть диалог
    },
    onInfoTap = {
        Releazio.openPostURL(updateState)
    }
)
```

#### VersionView
Компонент для отображения версии приложения с кнопкой обновления.

```kotlin
VersionView(
    updateState = updateState,
    onUpdateTap = {
        Releazio.openAppStore(updateState)
    }
)
```

#### BadgeView
Бейдж-индикатор для новых обновлений.

```kotlin
BadgeView(
    text = "New",
    backgroundColor = Color.Yellow,
    textColor = Color.Black
)
```

### View System (Legacy)

#### UpdateDialogFragment
```kotlin
val dialog = UpdateDialogFragment.newInstance(updateState)
dialog.show(supportFragmentManager, "update_dialog")
```

## 🔧 API Reference

### Основные методы

#### `checkUpdates() suspend -> UpdateState`
Главный метод для проверки обновлений. Возвращает `UpdateState` с полной информацией о состоянии обновлений.

```kotlin
val updateState = Releazio.checkUpdates()
```

#### `openAppStore(updateState: UpdateState) -> Boolean`
Открывает Play Store для обновления приложения.

```kotlin
Releazio.openAppStore(updateState)
```

#### `openPostURL(updateState: UpdateState) -> Boolean`
Открывает URL поста (для type 0 или при клике на кнопку информации).

```kotlin
Releazio.openPostURL(updateState)
```

#### `markPostAsOpened(postURL: String)`
Отмечает пост как открытый (для type 0 badge logic).

```kotlin
Releazio.markPostAsOpened(postURL)
```

#### `markPopupAsShown(version: String, updateType: Int)`
Отмечает попап как показанный (для type 2, 3).

```kotlin
Releazio.markPopupAsShown(
    version = updateState.currentVersion,
    updateType = updateState.updateType
)
```

#### `skipUpdate(version: String) -> Int`
Пропускает обновление и возвращает количество оставшихся попыток (для type 3).

```kotlin
val remaining = Releazio.skipUpdate(updateState.currentVersion)
```

### UpdateState

Структура, возвращаемая методом `checkUpdates()`, содержит:

- `updateType: Int` — Тип обновления (0, 1, 2, 3)
- `shouldShowBadge: Boolean` — Показывать ли бейдж (type 0)
- `shouldShowPopup: Boolean` — Показывать ли попап (type 2, 3)
- `shouldShowUpdateButton: Boolean` — Показывать ли кнопку обновления (type 1)
- `remainingSkipAttempts: Int` — Осталось пропусков (type 3)
- `channelData: ChannelData` — Полные данные из API
- `badgeURL: String?` — URL для открытия при клике на бейдж
- `updateURL: String?` — URL для обновления приложения
- `currentVersionName: String` — Текущая версия приложения (для отображения)
- `latestVersionName: String` — Последняя доступная версия (для отображения)
- `isUpdateAvailable: Boolean` — Доступно ли обновление

## ⚙️ Конфигурация

### ReleazioConfiguration

```kotlin
val configuration = ReleazioConfiguration(
    apiKey = "your-api-key",                      // Обязательно
    debugLoggingEnabled = false,                   // По умолчанию: false
    networkTimeout = 30_000L,                      // По умолчанию: 30 секунд
    analyticsEnabled = true,                       // По умолчанию: true
    cacheExpirationTime = 3600L,                   // По умолчанию: 3600 секунд (1 час)
    locale = "en",                                 // По умолчанию: "en" (поддерживается "ru")
    badgeColor = Color.Yellow                      // По умолчанию: null (system yellow)
)
```

### Параметры

- **apiKey** — API ключ для аутентификации (обязательно)
- **debugLoggingEnabled** — Включить отладочные логи
- **networkTimeout** — Таймаут сетевых запросов в миллисекундах
- **analyticsEnabled** — Включить сбор аналитики
- **cacheExpirationTime** — Время жизни кэша в секундах
- **locale** — Локаль для локализации ("en" или "ru")
- **badgeColor** — Кастомный цвет бейджа (опционально)

## 🌍 Локализация

SDK поддерживает два языка:

- **en** — Английский
- **ru** — Русский

Локализованные строки:
- `releazio_update_title` — Заголовок диалога обновления
- `releazio_update_message` — Сообщение об обновлении
- `releazio_button_update` — Текст кнопки "Обновить"
- `releazio_button_skip` — Текст кнопки "Пропустить"
- `releazio_button_close` — Текст кнопки "Закрыть"
- `releazio_badge_new` — Текст бейджа "Новое"
- `releazio_changelog_title` — Текст "Что нового"

## 📖 Документация

Полный пример интеграции доступен в папке [Example](./example/).

## 💡 Примеры использования

### Пример полной интеграции (Jetpack Compose)

```kotlin
@Composable
fun MyApp() {
    var updateState by remember { mutableStateOf<UpdateState?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        try {
            updateState = Releazio.checkUpdates()
            
            // Показываем диалог если нужно
            if (updateState?.shouldShowPopup == true) {
                showUpdateDialog = true
            }
        } catch (e: ReleazioError) {
            Log.e("MyApp", "Ошибка: ${e.message}")
        }
    }
    
    // Ваш контент
    
    // Версия и кнопка обновления
    updateState?.let { state ->
        VersionView(
            updateState = state,
            onUpdateTap = {
                Releazio.openAppStore(state)
            }
        )
    }
    
    // Диалог обновления
    if (showUpdateDialog && updateState != null) {
        ReleazioUpdateDialog(
            updateState = updateState!!,
            style = DialogStyle.Native,
            onUpdate = {
                Releazio.openAppStore(updateState!!)
                showUpdateDialog = false
            },
            onSkip = { remaining ->
                Releazio.skipUpdate(updateState!!.currentVersion)
                if (remaining == 0) {
                    showUpdateDialog = false
                }
            },
            onDismiss = {
                Releazio.markPopupAsShown(
                    version = updateState!!.currentVersion,
                    updateType = updateState!!.updateType
                )
                showUpdateDialog = false
            },
            onInfoTap = {
                Releazio.openPostURL(updateState!!)
            }
        )
    }
}
```

## 🐛 Обработка ошибок

SDK использует `ReleazioError` для обработки ошибок:

```kotlin
try {
    val updateState = Releazio.checkUpdates()
} catch (e: ReleazioError) {
    when (e) {
        is ReleazioError.ConfigurationMissing -> {
            Log.e("Releazio", "SDK не настроен")
        }
        is ReleazioError.NetworkError -> {
            Log.e("Releazio", "Ошибка сети: ${e.cause}")
        }
        is ReleazioError.ApiError -> {
            Log.e("Releazio", "API ошибка: ${e.code} - ${e.message}")
        }
        else -> {
            Log.e("Releazio", "Неизвестная ошибка: ${e.message}")
        }
    }
}
```

## 🤝 Поддержка

- 📧 Email: support@releazio.com
- 🐛 Issues: [GitHub Issues](https://github.com/Releazio/releazio-sdk-android/issues)
- 📖 Документация: [Releazio Docs](https://releazio.com/docs)

## 📄 Лицензия

Releazio Android SDK доступен под лицензией MIT. Смотрите [LICENSE](LICENSE) для деталей.

---

**Сделано с ❤️ командой Releazio**


