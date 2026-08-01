# Multi Colors

**Multi Colors** is a professional and reactive theme management library for Android. It allows you to easily implement and switch between multiple themes (colors and gradients) in your application with automatic persistence and UI updates.

## Features

- ✅ **Reactive Architecture**: Built with Kotlin Flow and Coroutines for real-time theme updates.
- ✅ **Automatic Persistence**: Saves the user's selected theme using Jetpack DataStore.
- ✅ **Gradient Support**: Full support for both solid colors and gradient themes.
- ✅ **Dynamic Themes**: Register themes directly from Kotlin code without XML.
- ✅ **Modular Registry**: Separate Registry for managing themes cleanly.

## Installation

Add it to your root `build.gradle` or `settings.gradle`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your app's `build.gradle`:

```kotlin
dependencies {
    implementation("com.github.selimdawa:MultiColors:x.y.z")
}
```

## Usage

### 1. Initialize and Register Themes

In your `Application` class:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 1. Register Kotlin-based (Dynamic) themes
        ThemeRegistry.register(
            MultiColorTheme.Dynamic(
                id = "MY_COLOR",
                name = "Ocean Blue",
                backgroundColor = Color.parseColor("#0077CC")
            )
        )

        // 2. Register XML-based themes (Optional)
        ThemeRegistry.register(
            MultiColorTheme.Xml(
                id = "NIGHT_MODE",
                name = "Night",
                styleRes = R.style.MyNightTheme
            )
        )

        // 3. Initialize the manager
        MultiColorManager.init(this)
    }
}
```

### 2. Add to your Layout

Use the `MultiColorButton` to let users switch themes:

```xml
<io.selimdawa.multicolors.MultiColorButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

## Customization

You can replace all default themes by clearing the registry or simply adding your own. The library components will automatically react to any theme registered in `ThemeRegistry`.

## License

```
Copyright 2026 Selim Dawa

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
