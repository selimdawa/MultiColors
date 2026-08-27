package com.flatcode.multicolors

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import io.selimdawa.multicolors.MultiColorNightModeButton
import io.selimdawa.multicolors.NightModeAnimationHelper

/**
 * Base activity that handles theme initialization and shared UI logic.
 */
open class BaseActivity : AppCompatActivity() {

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setupThemeToggle()
    }

    private fun setupThemeToggle() {
        // MultiColorNightModeButton handles its own clicks and animations.
        // This is a fallback for regular ImageViews used as theme toggles.
        val btnThemeToggle = findViewById<ImageView>(R.id.btnThemeToggle)
        if (btnThemeToggle != null && btnThemeToggle !is MultiColorNightModeButton) {
            btnThemeToggle.setOnClickListener {
                toggleNightMode(it)
            }
        }
    }

    protected fun toggleNightMode(view: View? = null) {
        if (view != null) {
            val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES
            
            // Set reverse animation (OUTWARD) when switching TO night mode
            val animationType = if (isNightMode) 
                NightModeAnimationHelper.AnimationType.INWARD else NightModeAnimationHelper.AnimationType.OUTWARD

            NightModeAnimationHelper.performAnimatedAction(
                this, view, animationType
            ) {
                performNightModeChange()
            }
        } else {
            performNightModeChange()
        }
    }

    private fun performNightModeChange() {
        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        AppCompatDelegate.setDefaultNightMode(
            if (isNightMode) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
        )
    }
}