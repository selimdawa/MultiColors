package com.flatcode.multicolors

import android.app.Application
import android.graphics.Color
import io.selimdawa.multicolors.MultiColorManager
import io.selimdawa.multicolors.MultiColorTheme
import io.selimdawa.multicolors.ThemeRegistry

class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        //Registering themes directly from Kotlin (Dynamic Colors) - Using attrs names
        //ThemeRegistry.register(MultiColorTheme.Dynamic("KOTLIN_PURPLE", "Kotlin Purple", Color.parseColor("#9C27B0")))
        //ThemeRegistry.register(MultiColorTheme.Dynamic("KOTLIN_RED", "Kotlin Red", Color.RED))
        ThemeRegistry.register(MultiColorTheme.Dynamic("BLUE", "Ocean Blue", Color.BLUE))
        ThemeRegistry.register(MultiColorTheme.Dynamic("RED", "Flaming Red", Color.RED))
        ThemeRegistry.register(MultiColorTheme.Dynamic("GREEN", "Nature Green", Color.GREEN))
        ThemeRegistry.register(MultiColorTheme.Dynamic("PURPLE", "Royal Purple", Color.parseColor("#9C27B0")))
        ThemeRegistry.register(MultiColorTheme.Dynamic("ORANGE", "Sunset Orange", Color.parseColor("#FF9800")))
        ThemeRegistry.register(MultiColorTheme.Dynamic("TEAL", "Teal Breeze", Color.parseColor("#009688")))
        ThemeRegistry.register(MultiColorTheme.Dynamic("DARK_GRAY", "Midnight Gray", Color.DKGRAY))

        MultiColorManager.init(this)
    }
}