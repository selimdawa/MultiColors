# Multi Colors 🎨

<div style="text-align: center;">
  <b>A professional and reactive theme management library for Android.</b>
</div>

<div style="text-align: center;">
  Multi Colors allows you to easily implement and switch between multiple themes (colors and gradients) in your application with automatic persistence, smooth UI transitions, and full support for both XML Views and Jetpack Compose.
</div>

<div style="text-align: center;">
 <a><img alt="Min SDK" src="https://img.shields.io/badge/Min SDK-24-020290?logo=android&logoColor=white"/></a>
 <a><img alt="Target SDK" src="https://img.shields.io/badge/Target SDK-37-0EB265?logo=android&logoColor=0EB265"/></a>
 <a href="https://kotlinlang.org"><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.4.20-blue?logo=kotlin&logoColor=white"/></a>
 <a href="https://www.apache.org/licenses/LICENSE-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache_2.0-CC9900?logo=apache&logoColor=white"/></a>
</div>

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
    android:layout_width="48dp"
    android:layout_height="48dp"
    android:contentDescription="Open Theme Selector"
    style="@style/Widget.Material3.Button.IconButton" />

<!-- 🆕 MultiColorNightModeButton: Toggles Light/Dark mode with premium circular reveal -->
<io.selimdawa.multicolors.MultiColorNightModeButton
    android:layout_width="48dp"
    android:layout_height="48dp"
    app:mc_light_icon="@drawable/ic_sun"
    app:mc_dark_icon="@drawable/ic_moon"
    app:mc_icon_color_mode="adaptive" /> <!-- 'adaptive' (Black/White) or 'track' (Theme Color) -->

<!-- 🆕 MultiColorAvatarView: Profile image with rotating neon border and all premium features -->
<io.selimdawa.multicolors.MultiColorAvatarView
    android:layout_width="120dp"
    android:layout_height="120dp"
    app:mc_image_src="@drawable/profile_pic"
    app:mc_image_background="?mc_track"
    app:mc_image_scale_type="centerCrop"
    app:mc_image_corner_radius="60dp"
    app:mc_animate_border="true"
    app:mc_border_rotation_duration="3000"
    app:mc_border_rotation_direction="clockwise"
    app:mc_animate_image="false"
    app:mc_image_rotation_duration="5000"
    app:mc_border_thickness="4dp"
    app:mc_glow_radius="10dp"
    app:mc_glow_alpha="0.6"
    app:mc_use_rainbow="false"
    app:mc_show_contrast="true"
    app:mc_contrast_size="0.3"
    app:mc_always_white="false" />

<!-- 🆕 MultiColorCardView: Premium Card with reactive theme background and adaptive corners -->
<io.selimdawa.multicolors.MultiColorCardView
    android:layout_width="match_parent"
    android:layout_height="200dp"
    app:mc_card_background="?mc_bg"
    app:mc_card_corner_radius="24dp"
    app:mc_card_elevation="4dp"
    app:mc_card_border_enabled="true"
    app:mc_card_stroke_width="2dp"
    app:mc_card_stroke_color="?mc_tick" />

<!-- 🆕 MultiColorBorderLayout: Container with animated rotating borders -->
<io.selimdawa.multicolors.MultiColorBorderLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:mc_animate_border="true"
    app:mc_border_thickness="3dp"
    app:mc_corner_radius="16dp"
    app:mc_glow_radius="8dp"
    app:mc_glow_alpha="0.5"
    app:mc_border_rotation_duration="4000"
    app:mc_border_rotation_direction="counter_clockwise">
    
    <!-- Your content here -->
    <TextView 
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Premium Container" />
    
</io.selimdawa.multicolors.MultiColorBorderLayout>

<!-- 🆕 RedBlueBorderLayout: Specialized container with rotating Red/Blue neon border -->
<io.selimdawa.multicolors.RedBlueBorderLayout
    android:layout_width="200dp"
    android:layout_height="wrap_content"
    app:mc_border_thickness="5dp"
    app:mc_glow_radius="12dp"
    app:mc_corner_radius="20dp" />
```

---

## ⚙️ Configuration & Initialization

In your `Application` class:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. Register Custom Themes (Optional)
        ThemeRegistry.register(
            MultiColorTheme(
                id = "GOLDEN",
                name = "Golden Hour",
                colors = listOf(Color.parseColor("#FFD700"), Color.parseColor("#FF8C00"))
            )
        )

        // 2. Configure Manager Settings
        MultiColorManager.apply {
            excludedThemeIds = setOf("S_12", "G2_5") // Hide specific themes from the UI
            isThemeSafeModeEnabled = true           // Fallback to default on crash
        }

        // 3. Initialize (Handles persistence and lifecycle)
        MultiColorManager.init(this)
        
        // 4. Preload for zero-lag switching (Intelligent IdleHandler)
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

### Global & Shared Attributes
| Attribute             | Description                                            | Default              |
|:----------------------|:-------------------------------------------------------|:---------------------|
| `mc_bg`               | Theme background attribute (reference or color)        | `?attr/colorSurface` |
| `mc_track`            | Theme primary/start color                              | `-`                  |
| `mc_center`           | Theme center color (for 3-color gradients)             | `-`                  |
| `mc_tick`             | Theme accent/end color                                 | `-`                  |
| `mc_border_thickness` | Thickness of the colorful border                       | `2dp`                |
| `mc_use_rainbow`      | Forces rainbow colors instead of current theme         | `false`              |
| `mc_always_white`     | Forces contrast stripes to be white regardless of mode | `false`              |
| `mc_show_contrast`    | Enables high-contrast center stripe in the border      | `false`              |
| `mc_contrast_size`    | Width of the contrast stripe (0.0 to 1.0)              | `0.3`                |

### MultiColorAvatarView & MultiColorBorderLayout
| Attribute                      | Description                                     | Default      |
|:-------------------------------|:------------------------------------------------|:-------------|
| `mc_animate_border`            | Enables/Disables border rotation animation      | `false`      |
| `mc_border_rotation_duration`  | Time (ms) for a full 360° border rotation       | `3000`       |
| `mc_border_rotation_direction` | `clockwise` or `counter_clockwise`              | `clockwise`  |
| `mc_glow_radius`               | Radius of the neon glow effect                  | `0dp`        |
| `mc_glow_alpha`                | Alpha intensity of the glow (0.0 to 1.0)        | `0.5`        |
| `mc_corner_radius`             | Corner radius for layouts and borders           | `8dp`        |
| `mc_image_src`                 | Profile image resource (Avatar only)            | `-`          |
| `mc_image_background`          | Background color for the image (Avatar only)    | `-`          |
| `mc_image_scale_type`          | Scale type (centerCrop, fitXY, etc.)            | `centerCrop` |
| `mc_animate_image`             | Enables rotating the image itself (Avatar only) | `false`      |
| `mc_image_rotation_duration`   | Time (ms) for image rotation (Avatar only)      | `5000`       |

### MultiColorCardView
| Attribute                | Description                               | Default       |
|:-------------------------|:------------------------------------------|:--------------|
| `mc_card_background`     | Background color or resource for the card | `?mc_bg`      |
| `mc_card_corner_radius`  | Corner radius dimension or `circle`       | `10dp`        |
| `mc_card_elevation`      | Card elevation (shadow)                   | `0dp`         |
| `mc_card_border_enabled` | Enables the stroke border                 | `false`       |
| `mc_card_stroke_width`   | Thickness of the border stroke            | `2dp`         |
| `mc_card_stroke_color`   | Color of the border stroke                | `Color.WHITE` |

### MultiColorNightModeButton
| Attribute            | Description                               | Default    |
|:---------------------|:------------------------------------------|:-----------|
| `mc_light_icon`      | Icon to show during Light Mode (Sun)      | `-`        |
| `mc_dark_icon`       | Icon to show during Dark Mode (Moon)      | `-`        |
| `mc_icon_color_mode` | `track` (theme color) or `adaptive` (B/W) | `adaptive` |

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
