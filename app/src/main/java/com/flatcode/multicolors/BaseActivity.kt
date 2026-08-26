package com.flatcode.multicolors

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import io.selimdawa.multicolors.MultiColorNightModeButton
import io.selimdawa.multicolors.ThemeAnimationHelper

open class BaseActivity : AppCompatActivity() {

    private var lastClickTime: Long = 0
    private val clickInterval: Long = 2000 // Prevent double clicking for 2 seconds

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setupThemeToggle()
    }

    private fun setupThemeToggle() {
        // No manual setup needed for MultiColorNightModeButton
        // but we still update icons for regular ImageViews if needed
        val btnThemeToggle = findViewById<ImageView>(R.id.btnThemeToggle)
        if (btnThemeToggle != null && btnThemeToggle !is MultiColorNightModeButton) {
            btnThemeToggle.setOnClickListener {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime > clickInterval) {
                    lastClickTime = currentTime
                    toggleNightMode(it)
                }
            }
        }
    }

    protected fun toggleNightMode(view: View? = null) {
        if (view != null) {
            ThemeAnimationHelper.shouldAnimateThemeIcon = true
            ThemeAnimationHelper.performAnimatedAction(this, view) {
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

    private fun updateThemeIcon(imageView: ImageView?) {
        // MultiColorNightModeButton handles its own icon logic in onAttachedToWindow
        if (imageView == null || imageView is MultiColorNightModeButton) return

        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        
        imageView.setImageResource(
            if (isNightMode) R.drawable.ic_light else R.drawable.ic_night
        )
    }
}