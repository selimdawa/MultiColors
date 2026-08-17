package com.flatcode.multicolors

import android.app.Application
import io.selimdawa.multicolors.MultiColorManager

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        //MultiColorManager.excludedThemeIds = setOf("G2_1")
        MultiColorManager.init(this)
    }
}