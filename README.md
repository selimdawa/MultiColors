# Multi Colors 🎨

**Multi Colors** is a professional and reactive theme management library for Android. It allows you to easily implement and switch between multiple themes (colors and gradients) in your application with automatic persistence and smooth UI transitions.

## Features

- ✅ **Smooth Transitions**: Circular reveal animations for a premium user experience when changing themes. Now usable for ANY action (like Night Mode toggle).
- ✅ **MultiColorNightModeButton**: Specialized button with Telegram-style animations that requires custom sun/moon icons from the app.
- ✅ **Automatic Persistence**: Saves the user's selected theme using Jetpack DataStore.
- ✅ **Memory Optimized**: Automatic bitmap recycling and lifecycle-aware collectors to prevent memory leaks.
- ✅ **Advanced Preloading**: Uses `IdleHandler` to preload theme backgrounds for zero-lag UI.
- ✅ **Custom Color Picker**: Integrated UI for users to create and save their own solid color themes.
- ✅ **Theme Management**: Built-in dialog to manage, hide, or prioritize themes in the selection list.
- ✅ **Safe Mode**: Automatic fallback to a default theme if registration errors or resource issues occur.
- ✅ **Unified Theme API**: Simplified theme registration with support for XML styles or programmatic gradients.
- ✅ **Edge-to-Edge Ready**: Built-in support for status and navigation bar color synchronization.
- ✅ **New UI Components**: `MultiColorAvatarView`, `MultiColorBorderLayout`, and `RedBlueBorderLayout` for stunning visual effects.
- ✅ **Animated Borders**: Rotating gradient borders with customizable speed, direction, and neon glow.
- ✅ **Rainbow Mode**: Optional rainbow color cycle for borders independent of the current theme.

## Installation

Add JitPack to your root `settings.gradle`:

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
        
        // 1. (Optional) Configure Manager
        MultiColorManager.isThemeSafeModeEnabled = true
        MultiColorManager.excludedThemeIds = setOf("SOME_ID")

        // 2. Register Custom Themes
        ThemeRegistry.register(
            MultiColorTheme(
                id = "SUNSET",
                name = "Sunset",
                colors = listOf(Color.RED, Color.YELLOW),
                orientation = GradientDrawable.Orientation.TOP_BOTTOM
            )
        )

        // 3. Initialize the manager
        MultiColorManager.init(this)
    }
}
```

### 2. Add to your Layout

Use `MultiColorButton` for an automated theme selector, or `MultiColorView` for a themed reactive container:

```xml
<!-- Clickable button that automatically opens the theme management dialog -->
<io.selimdawa.multicolors.MultiColorButton
    android:layout_width="34dp"
    android:layout_height="34dp" />

<!-- 🆕 MultiColorNightModeButton: Specialized button for Night/Light mode -->
<io.selimdawa.multicolors.MultiColorNightModeButton
    android:layout_width="34dp"
    android:layout_height="34dp"
    app:mc_dark_icon="@drawable/ic_night"
    app:mc_light_icon="@drawable/ic_light" />

<!-- A view (MaterialCardView-based) that reacts to theme changes -->
<io.selimdawa.multicolors.MultiColorView
    android:layout_width="match_parent"
    android:layout_height="200dp"
    app:cardCornerRadius="16dp" />

<!-- 🆕 MultiColorAvatarView: Profile image with rotating colorful border -->
<io.selimdawa.multicolors.MultiColorAvatarView
    android:layout_width="100dp"
    android:layout_height="100dp"
    app:mc_animate_border="true"
    app:mc_border_thickness="4dp"
    app:mc_glow_radius="8dp"
    app:mc_image_src="@drawable/my_profile" />

<!-- 🆕 MultiColorBorderLayout: A container with a rotating colorful border -->
<io.selimdawa.multicolors.MultiColorBorderLayout
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:mc_animate_border="true"
    app:mc_border_thickness="2dp"
    app:mc_corner_radius="12dp">
    
    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Premium Button" />
        
</io.selimdawa.multicolors.MultiColorBorderLayout>

<!-- 🆕 RedBlueBorderLayout: A specialized container with a rotating Red/Blue neon border -->
<io.selimdawa.multicolors.RedBlueBorderLayout
    android:layout_width="200dp"
    android:layout_height="wrap_content"
    app:mc_border_rotation_duration="2000"
    app:mc_border_thickness="5dp"
    app:mc_glow_radius="12dp" />
```

## Advanced APIs

### Programmatic Control
Change the theme manually from anywhere in your code:
```kotlin
MultiColorManager.showThemeDialog(activity) // Opens the selector
// OR
MultiColorManager.showColorPickerDialog(activity) // Opens the custom picker
```

### Exclude Themes
Hide specific default themes from the user:
```kotlin
MultiColorManager.excludedThemeIds = setOf("GRADUAL_ONE", "SOLID_WHITE")
```

### 🆕 Universal Animated Action
You can now use the library's premium circular reveal animation for any UI change (like switching to Night Mode or changing Languages):

```kotlin
ThemeAnimationHelper.performAnimatedAction(activity, triggerView) {
    // 1. Perform your UI change (e.g. toggle night mode)
    toggleNightMode()
    // 2. The library will take a screenshot, calculate reveal center from triggerView, 
    // and perform a smooth transition!
}
```

## XML Attributes

Customizable attributes for `MultiColorAvatarView` and `MultiColorBorderLayout`:

| Attribute                     | Description                               | Default       |
|-------------------------------|-------------------------------------------|---------------|
| `mc_animate_border`           | Enables/Disables border rotation          | `false`       |
| `mc_border_thickness`         | Thickness of the colorful border          | `4dp` / `2dp` |
| `mc_glow_radius`              | Adds a neon glow effect around the border | `0dp`         |
| `mc_border_rotation_duration` | Time (ms) for a full 360° rotation        | `3000`        |
| `mc_use_rainbow`              | Force rainbow colors instead of theme     | `false`       |
| `mc_image_corner_radius`      | Corner radius for the avatar image        | `Pill`        |

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
