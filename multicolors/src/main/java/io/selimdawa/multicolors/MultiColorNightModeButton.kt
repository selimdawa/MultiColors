package io.selimdawa.multicolors

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageView

/**
 * A specialized button for toggling Night/Light mode with built-in 
 * Telegram-style animations and automatic theme handling.
 */
class MultiColorNightModeButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var lightIconRes: Int = 0
    private var darkIconRes: Int = 0
    private var lastClickTime: Long = 0
    private val clickInterval: Long = 2000 // Prevent double clicking for 2 seconds

    init {
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.MultiColorNightModeButton, 0, 0
        ).apply {
            try {
                lightIconRes = getResourceId(R.styleable.MultiColorNightModeButton_mc_light_icon, 0)
                darkIconRes = getResourceId(R.styleable.MultiColorNightModeButton_mc_dark_icon, 0)
            } finally {
                recycle()
            }
        }

        setupClickListener()
        updateIcon(false) // Initial state without animation
    }

    private fun setupClickListener() {
        setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > clickInterval) {
                lastClickTime = currentTime
                toggleNightMode()
            }
        }
    }

    private fun toggleNightMode() {
        val activity = findActivity(context) ?: return
        
        ThemeAnimationHelper.shouldAnimateThemeIcon = true
        ThemeAnimationHelper.performAnimatedAction(activity, this) {
            val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES
            
            AppCompatDelegate.setDefaultNightMode(
                if (isNightMode) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
            )
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateIcon(ThemeAnimationHelper.shouldAnimateThemeIcon)
        if (ThemeAnimationHelper.shouldAnimateThemeIcon) {
            ThemeAnimationHelper.shouldAnimateThemeIcon = false
        }
    }

    private fun updateIcon(animate: Boolean) {
        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        
        if (animate) {
            rotation = if (isNightMode) 180f else -180f
            animate()
                .rotation(0f)
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(500)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
                }
                .start()
        } else {
            rotation = 0f
            scaleX = 1.0f
            scaleY = 1.0f
        }

        val iconRes = if (isNightMode) lightIconRes else darkIconRes
        if (iconRes != 0) {
            setImageResource(iconRes)
        }
    }

    private fun findActivity(context: Context): Activity? {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }
}