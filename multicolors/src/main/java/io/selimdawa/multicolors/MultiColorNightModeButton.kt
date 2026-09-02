package io.selimdawa.multicolors

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A specialized button for toggling Night/Light mode with built-in 
 * Telegram-style animations and automatic theme handling.
 */
class MultiColorNightModeButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var lightIconRes: Int = 0
    private var darkIconRes: Int = 0
    private var iconColorMode: Int = 1 // Default: adaptive
    private var themeJob: Job? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.MultiColorNightModeButton, 0, 0
        ).apply {
            try {
                lightIconRes = getResourceId(R.styleable.MultiColorNightModeButton_mc_light_icon, 0)
                darkIconRes = getResourceId(R.styleable.MultiColorNightModeButton_mc_dark_icon, 0)
                iconColorMode = getInt(R.styleable.MultiColorNightModeButton_mc_icon_color_mode, 1)
            } finally {
                recycle()
            }
        }

        setupClickListener()
        updateIcon(false) // Initial state without animation
    }

    private fun setupClickListener() {
        setOnClickListener {
            toggleNightMode()
        }
    }

    private fun toggleNightMode() {
        val activity = findActivity(context) ?: return

        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

        if (isNightMode) {
            // Sun spinning out for 400ms
            animate()
                .rotation(180f)
                .setDuration(400)
                .setInterpolator(DecelerateInterpolator())
                .start()
            
            // Give 100ms head start before capturing screenshot
            postDelayed({
                performNightModeTransition(activity, isNightMode)
            }, 100)
        } else {
            // Start transition IMMEDIATELY in parallel for Moon
            performNightModeTransition(activity, isNightMode)
        }
    }

    private fun performNightModeTransition(activity: Activity, isNightMode: Boolean) {
        val animationType = if (isNightMode)
            NightModeAnimationHelper.AnimationType.INWARD else NightModeAnimationHelper.AnimationType.OUTWARD

        NightModeAnimationHelper.performAnimatedAction(
            activity, this, animationType
        ) {
            val newMode = if (isNightMode) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
            MultiColorManager.setNightMode(context, newMode)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val shouldAnimate = NightModeAnimationHelper.isTransitioning
        updateIcon(shouldAnimate)

        if (iconColorMode == 0) { // track mode
            themeJob?.cancel()
            themeJob = findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
                MultiColorManager.currentThemeId.collectLatest {
                    val activity = findActivity(context)
                    // PREVENT FLASH: Do not update views in the old activity
                    if (activity != null && activity.isFinishing) return@collectLatest
                    updateIcon(false)
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        themeJob?.cancel()
        themeJob = null
        super.onDetachedFromWindow()
    }

    private fun updateIcon(animate: Boolean) {
        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

        applyIconColor(isNightMode)

        val iconRes = if (isNightMode) lightIconRes else darkIconRes
        if (iconRes != 0) {
            setImageResource(iconRes)
        }

        if (animate) {
            if (isNightMode) {
                // Sun incoming: Synchronized Rotation (400ms)
                rotation = -180f
                alpha = 0f
                scaleX = 0.5f
                scaleY = 0.5f
            } else {
                // Moon incoming: No rotation, just scale/fade pop
                rotation = 0f
                alpha = 0f
                scaleX = 0.8f
                scaleY = 0.8f
            }

            post {
                if (isNightMode) {
                    animate()
                        .rotation(0f)
                        .alpha(1f)
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(400)
                        .setInterpolator(DecelerateInterpolator())
                        .withEndAction {
                            animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start()
                        }
                        .start()
                } else {
                    animate()
                        .alpha(1f)
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(400)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }
            }
        } else {
            rotation = 0f
            scaleX = 1.0f
            scaleY = 1.0f
            alpha = 1f
        }
        translationY = 0f
    }

    private fun applyIconColor(isNightMode: Boolean) {
        if (iconColorMode == 0) { // track mode
            val theme = MultiColorManager.getCurrentTheme(context)
            val colors = MultiColorManager.getThemeColors(context, theme)
            if (colors.isNotEmpty()) {
                setColorFilter(colors[0], PorterDuff.Mode.SRC_IN)
            }
        } else { // adaptive mode
            val color = if (isNightMode) Color.WHITE else Color.BLACK
            setColorFilter(color, PorterDuff.Mode.SRC_IN)
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