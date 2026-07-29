package com.flatcode.multicolors

import android.app.Application
import io.selimdawa.multicolors.MultiColorManager

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Example: Registering a custom theme from the app
        MultiColorManager.registerTheme(
            id = "CUSTOM_RED",
            styleRes = R.style.Theme_App_CustomRed,
            name = "Custom Red"
        )
        
        MultiColorManager.init(this)
    }
}