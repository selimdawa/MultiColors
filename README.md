# Multi Colors

**Multi Colors** is a professional and reactive theme management library for Android. It allows you to easily implement and switch between multiple themes (colors and gradients) in your application with automatic persistence and UI updates.

## Features

- ✅ **Reactive Architecture**: Built with Kotlin Flow and Coroutines for real-time theme updates.
- ✅ **Automatic Persistence**: Saves the user's selected theme using Jetpack DataStore.
- ✅ **Gradient Support**: Full support for both solid colors and gradient themes.
- ✅ **Pre-built Components**: Includes `MultiColorButton` and a customizable selection dialog.
- ✅ **Easy Integration**: Automatically handles Activity recreation and theme application.

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

### 1. Initialize in Application Class

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MultiColorManager.init(this)
    }
}
```

### 2. Add to your Layout

You can use the built-in `MultiColorButton` which opens the theme selection dialog automatically:

```xml
<io.selimdawa.multicolors.MultiColorButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

### 3. Show Dialog Manually

If you want to trigger the dialog from a custom action:

```kotlin
MultiColorManager.showThemeDialog(activity)
```

## Customization

### Adding Custom Themes

You can add your own themes without replacing the default ones. 

1. Define your theme in `themes.xml` inheriting from `MC_Base_Theme`:
```xml
<style name="Theme.App.CustomRed" parent="MC_Base_Theme">
    <item name="mc_bg">#FF0000</item>
    <item name="mc_tick">#880000</item>
    <item name="mc_track">#FF4444</item>
</style>
```

2. Register it in your `Application` class before calling `init`:
```kotlin
MultiColorManager.registerTheme(
    id = "CUSTOM_RED",
    styleRes = R.style.Theme_App_CustomRed,
    name = "Custom Red"
)
MultiColorManager.init(this)
```

### Replacing All Themes

You can also provide your own theme map during initialization to replace default themes entirely:

```kotlin
val myThemes = mapOf(
    "LIGHT" to R.style.MyLightTheme,
    "DARK" to R.style.MyDarkTheme
)

MultiColorManager.init(this, myThemes)
```

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
