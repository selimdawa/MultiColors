package io.selimdawa.multicolors

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * A container that draws a rotating Red and Blue border around its content.
 * Supports XML attributes for thickness, corner radius, animation speed, and glow.
 */
class RedBlueBorderLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val borderView = RedBlueAnimatedBorderView(context, attrs).apply {
        id = NO_ID
        isClickable = false
        isFocusable = false
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    init {
        clipChildren = false
        clipToPadding = false

        context.theme.obtainStyledAttributes(
            attrs, R.styleable.MultiColorBorderLayout, 0, 0
        ).apply {
            try {
                val thickness = getDimension(R.styleable.MultiColorBorderLayout_mc_border_thickness, context.dpToPx(4f))
                val glowRadius = getDimension(R.styleable.MultiColorBorderLayout_mc_glow_radius, 0f)
                
                // Add padding to ensure border and glow are not clipped
                val padding = (thickness + (glowRadius * 1.5f)).toInt()
                setPadding(padding, padding, padding, padding)
            } finally {
                recycle()
            }
        }
        
        // Add the border view at index 0 (behind other children)
        addView(borderView, 0, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    /**
     * Set the animation speed (duration in milliseconds).
     */
    fun setAnimationSpeed(durationMs: Long) {
        borderView.setAnimationDuration(durationMs)
    }

    /**
     * Set the border thickness in DP.
     */
    fun setBorderThickness(thicknessDp: Float) {
        borderView.setBorderThickness(thicknessDp)
        updateInternalPadding()
    }

    /**
     * Set the corner radius in DP.
     */
    fun setCornerRadius(radiusDp: Float) {
        borderView.setCornerRadius(radiusDp)
    }
    
    /**
     * Set the glow radius in DP.
     */
    fun setGlowRadius(radiusDp: Float) {
        borderView.setGlowRadius(radiusDp)
        updateInternalPadding()
    }

    private fun updateInternalPadding() {
        // This is a simplification; ideally we'd store the latest values
        val padding = context.dpToPxInt(8f) 
        setPadding(padding, padding, padding, padding)
    }
}
