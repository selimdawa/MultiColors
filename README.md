# Multi Colors 🎨

<p align="center">
  <b>A professional and reactive theme management library for Android.</b>
</p>

<p align="center">
  Multi Colors allows you to easily implement and switch between multiple themes (colors and gradients) in your application with automatic persistence, smooth UI transitions, and full support for both XML Views and Jetpack Compose.
</p>

<p align="center">
 <a><img alt="Min SDK" src="https://img.shields.io/badge/Min SDK-24-020290?logo=android&logoColor=white"/></a>
 <a><img alt="Target SDK" src="https://img.shields.io/badge/Target SDK-37-0EB265?logo=android&logoColor=0EB265"/></a>
 <a href="https://kotlinlang.org"><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.4.20-blue?logo=kotlin&logoColor=white"/></a>
 <a href="https://www.apache.org/licenses/LICENSE-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache_2.0-CC9900?logo=apache&logoColor=white"/></a>
</p>

## ✨ Key Features

- 🚀 **Full Jetpack Compose Support**: Premium composables, modifiers, and theme providers designed for modern Android development.
- 🎭 **Telegram-Style Animations**: Specialized circular reveal transitions for theme changes and night mode toggling.
- 🌓 **Smart Night Mode**: Unified management for light/dark modes with persistence and smooth cross-fade animations.
- 🌈 **Dynamic Animated Borders**: Rotating gradient borders for Avatars and Layouts with customizable speed, direction, and neon glow.
- ⚡ **Zero-Lag Preloading**: Intelligent `IdleHandler` preloading to ensure theme switching is instantaneous.
- 🛠️ **Advanced Theme Registry**: Register themes from XML, programmatic gradients, or even dynamic network sources.
- 📱 **Edge-to-Edge Synergy**: Automatic synchronization with system bars (Status/Navigation) for a truly immersive experience.
- 💾 **DataStore Persistence**: Lightweight and reactive state management for theme and night mode settings.

---

## 📦 Installation

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

---

## 🚀 Usage: Jetpack Compose (Recommended)

### 1. Wrap your App
Provide the theme context to your composables:

```kotlin
MultiColorTheme { // Collects theme state automatically
    Surface(modifier = Modifier.fillMaxSize()) {
        MyContent()
    }
}
```

### 2. Use Premium Composables

```kotlin
// 🆕 Animated Avatar with Rotating Border
MultiColorAvatar(
    image = { AsyncImage(model = "...", contentDescription = null) },
    animateBorder = true,
    glowRadius = 8.dp,
    borderThickness = 4.dp
)

// 🆕 BorderBox for Premium Containers
MultiColorBorderBox(
    thickness = 2.dp,
    animate = true,
    cornerRadius = 12.dp
) {
    Text("Premium Content")
}

// 🆕 Night Mode Toggle with Circular Reveal
MultiColorNightModeButton(
    lightIconRes = R.drawable.ic_sun,
    darkIconRes = R.drawable.ic_moon
)
```

### 3. Reactive Modifiers
Make any standard composable reactive to theme changes:

```kotlin
Box(
    modifier = Modifier
        .size(100.dp)
        .multiColorBackground(CircleShape) // Automatically uses theme gradient
        .multiColorBorder(2.dp, CircleShape)
)
```

---

## 🏗️ Usage: XML Views

### Specialized Components
```xml
<!-- Clickable button that automatically opens the theme selector -->
<io.selimdawa.multicolors.MultiColorButton
    android:layout_width="40dp"
    android:layout_height="40dp" />

<!-- 🆕 MultiColorAvatarView: Profile image with rotating neon border -->
<io.selimdawa.multicolors.MultiColorAvatarView
    android:layout_width="120dp"
    android:layout_height="120dp"
    app:mc_animate_border="true"
    app:mc_animate_image="true"
    app:mc_glow_radius="10dp"
    app:mc_image_src="@drawable/profile" />

<!-- 🆕 MultiColorBorderLayout: Container with animated borders -->
<io.selimdawa.multicolors.MultiColorBorderLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:mc_border_thickness="3dp"
    app:mc_corner_radius="16dp"
    app:mc_animate_border="true">
    
    <TextView ... />
    
</io.selimdawa.multicolors.MultiColorBorderLayout>

<!-- 🆕 RedBlueBorderLayout: Specialized container with rotating Red/Blue neon border -->
<io.selimdawa.multicolors.RedBlueBorderLayout
    android:layout_width="200dp"
    android:layout_height="wrap_content"
    app:mc_border_thickness="5dp"
    app:mc_glow_radius="12dp" />
```

---

## ⚙️ Configuration & Initialization

In your `Application` class:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. Register Themes
        ThemeRegistry.register(
            MultiColorTheme(
                id = "GOLDEN",
                name = "Golden Hour",
                colors = listOf(Color.parseColor("#FFD700"), Color.parseColor("#FF8C00"))
            )
        )

        // 2. Initialize (Handles persistence and lifecycle)
        MultiColorManager.init(this)
        
        // 3. (Optional) Preload for zero-lag
        MultiColorManager.preloadThemesIdle(this)
    }
}
```

---

## 🛠️ Advanced APIs

### Programmatic Animation
Perform the premium circular reveal animation for *any* custom action (like changing language or toggling a setting):

```kotlin
ThemeAnimationHelper.performAnimatedAction(activity, triggerView) {
    // Perform your logic here
    // The library handles the screenshot and smooth reveal!
}
```

### Night Mode Management
```kotlin
// Toggle night mode with persistence
MultiColorManager.setNightMode(context, AppCompatDelegate.MODE_NIGHT_YES)

// Access current state
val isNight = MultiColorManager.nightMode.value == AppCompatDelegate.MODE_NIGHT_YES
```

### Theme Management Dialogs
```kotlin
MultiColorManager.showThemeDialog(activity)       // Simple selector
MultiColorManager.showManageThemesDialog(activity) // Advanced management (hide/show themes)
```

---

## 🎨 XML Attributes

| Attribute                     | Description                               | Default    |
|-------------------------------|-------------------------------------------|------------|
| `mc_animate_border`           | Enables/Disables border rotation          | `false`    |
| `mc_animate_image`            | Enables/Disables image rotation (Avatar)  | `false`    |
| `mc_border_thickness`         | Thickness of the colorful border          | `2dp`      |
| `mc_glow_radius`              | Adds a neon glow effect around the border | `0dp`      |
| `mc_border_rotation_duration` | Time (ms) for a full 360° rotation        | `3000`     |
| `mc_image_rotation_duration`  | Time (ms) for image rotation (Avatar)     | `5000`     |
| `mc_use_rainbow`              | Force rainbow colors instead of theme     | `false`    |
| `mc_corner_radius`            | Corner radius for layouts                 | `8dp`      |
| `mc_icon_color_mode`          | Icon color mode (`track` or `adaptive`)   | `adaptive` |

---

## 📄 License

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
