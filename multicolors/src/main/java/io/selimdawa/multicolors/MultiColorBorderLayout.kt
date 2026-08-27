@file:Suppress("unused")

package io.selimdawa.multicolors

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout

/**
 * A container that draws a rotating colorful border around its content.
 * Perfect for buttons, text fields, or any rectangular UI element.
 */
class MultiColorBorderLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val borderView = MultiColorRectBorderView(context, attrs).apply {
        id = NO_ID
        isClickable = false
        isFocusable = false
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }
    
    var isAnimatingBorder = false
        private set

    private var borderDuration = 3000L
    private var borderDirection = 1
    private var borderAnimator: ObjectAnimator? = null

    init {
        clipChildren = false
        clipToPadding = false
        
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.MultiColorBorderLayout, 0, 0
        ).apply {
            try {
                isAnimatingBorder = getBoolean(R.styleable.MultiColorBorderLayout_mc_animate_border, false)
                borderDuration = getInteger(R.styleable.MultiColorBorderLayout_mc_border_rotation_duration, 3000).toLong()
                borderDirection = getInt(R.styleable.MultiColorBorderLayout_mc_border_rotation_direction, 1)
                
                val thickness = getDimension(R.styleable.MultiColorBorderLayout_mc_border_thickness, context.dpToPx(2f))
                val glowRadius = getDimension(R.styleable.MultiColorBorderLayout_mc_glow_radius, 0f)
                
                // Add padding to ensure border and glow are not clipped by the container bounds
                val padding = (thickness + (glowRadius * 1.5f)).toInt()
                setPadding(padding, padding, padding, padding)

            } finally {
                recycle()
            }
        }

        addView(borderView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        updateAnimations()
    }

    private fun updateAnimations() {
        borderAnimator?.cancel()
        if (isAnimatingBorder) {
            val start = 0f
            val end = 360f * borderDirection
            borderAnimator = ObjectAnimator.ofFloat(borderView, "shaderRotation", start, end).apply {
                duration = borderDuration
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                start()
            }
        }
    }

    override fun onDetachedFromWindow() {
        borderAnimator?.cancel()
        super.onDetachedFromWindow()
    }

    fun setAnimateBorder(animate: Boolean) {
        isAnimatingBorder = animate
        updateAnimations()
    }

    fun setColors(colors: IntArray) {
        borderView.setColors(colors)
    }

    fun setColors(colors: List<Int>) {
        borderView.setColors(colors)
    }

    fun resetToThemeColors() {
        borderView.resetToThemeColors()
    }
}
