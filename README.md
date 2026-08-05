# Multi Colors

**Multi Colors** is a professional and reactive theme management library for Android. It allows you to easily implement and switch between multiple themes (colors and gradients) in your application with automatic persistence and UI updates.

## Features

- ✅ **Reactive Architecture**: Built with Kotlin Flow and Coroutines for real-time theme updates.
- ✅ **Automatic Persistence**: Saves the user's selected theme using Jetpack DataStore.
- ✅ **Gradient Support**: Full support for both solid colors and smooth 3-color gradients.
- ✅ **MultiColorView & Button**: Pre-built components that react to theme changes automatically.
- ✅ **Unified Theme API**: Simplified theme registration with a single data class.
- ✅ **Edge-to-Edge Ready**: Built-in support for status and navigation bar color synchronization.

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
        
        // 1. Register XML-based themes (Best for Gradients)
        ThemeRegistry.register(
            MultiColorTheme(
                id = "NIGHT_MODE",
                name = "Night",
                styleRes = R.style.Theme_MC_Black
            )
        )

        // 2. Register Programmatic Gradient themes
        ThemeRegistry.register(
            MultiColorTheme(
                id = "SUNSET",
                name = "Sunset",
                colors = listOf(Color.RED, Color.YELLOW, Color.BLUE),
                orientation = GradientDrawable.Orientation.TOP_BOTTOM
            )
        )

        // 3. Initialize the manager
        MultiColorManager.init(this)
    }
}
```

### 2. Add to your Layout

Use `MultiColorButton` for a clickable theme selector, or `MultiColorView` for a themed container:

```xml
<!-- Clickable button that opens the theme dialog -->
<io.selimdawa.multicolors.MultiColorButton
    android:layout_width="40dp"
    android:layout_height="40dp" />

<!-- A view that automatically updates its background color/gradient -->
<io.selimdawa.multicolors.MultiColorView
    android:layout_width="match_parent"
    android:layout_height="200dp"
    app:cardCornerRadius="16dp" />
```

## Customization

You can replace all default themes by clearing the registry or simply adding your own. The library components will automatically react to any theme change without needing any manual code in your Activities.

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
