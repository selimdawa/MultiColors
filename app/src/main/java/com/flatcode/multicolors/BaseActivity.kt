package com.flatcode.multicolors

import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

open class BaseActivity : AppCompatActivity() {

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setupThemeToggle()
    }

    private fun setupThemeToggle() {
        val btnThemeToggle = findViewById<ImageView>(R.id.btnThemeToggle)
        btnThemeToggle?.setOnClickListener {
            toggleNightMode()
        }
        updateThemeIcon(btnThemeToggle)
    }

    protected fun toggleNightMode() {
        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        AppCompatDelegate.setDefaultNightMode(
            if (isNightMode) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
        )
    }

    private fun updateThemeIcon(imageView: ImageView?) {
        imageView?.let {
            val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES
            it.setImageResource(
                if (isNightMode) R.drawable.ic_light else R.drawable.ic_night
            )
        }
    }
}