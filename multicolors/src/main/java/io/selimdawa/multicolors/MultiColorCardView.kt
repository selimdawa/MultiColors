@file:Suppress("unused")

package io.selimdawa.multicolors

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.withStyledAttributes
import com.google.android.material.card.MaterialCardView

/**
 * A custom CardView that automatically applies the theme's [3 Colors Gradient] background.
 * Supports customizing CornerRadius, Stroke, and Elevation via XML or code.
 */
class MultiColorCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialCardViewStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val bgView: View = View(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.MultiColorCardView, defStyleAttr, 0) {
            // Load custom attributes with sensible defaults
            radius = getDimension(
                R.styleable.MultiColorCardView_mc_card_corner_radius, context.dpToPx(10f)
            )
            cardElevation = getDimension(R.styleable.MultiColorCardView_mc_card_elevation, 0f)

            val borderEnabled =
                getBoolean(R.styleable.MultiColorCardView_mc_card_border_enabled, false)
            if (borderEnabled) {
                strokeWidth = getDimensionPixelSize(
                    R.styleable.MultiColorCardView_mc_card_stroke_width, context.dpToPxInt(2f)
                )
                strokeColor =
                    getColor(R.styleable.MultiColorCardView_mc_card_stroke_color, Color.WHITE)
            } else {
                strokeWidth = 0
            }
        }

        // MaterialCardView specific settings
        preventCornerOverlap = true
        setCardBackgroundColor(Color.TRANSPARENT)

        // Update the background from the theme
        updateThemeBackground()

        // Add at index 0 to be behind content
        addView(bgView, 0)
    }

    /**
     * Forces the background to update based on the current MultiColor theme.
     */
    fun updateThemeBackground() {
        val typedValue = TypedValue()
        if (context.theme.resolveAttribute(R.attr.mc_bg, typedValue, true)) {
            if (typedValue.resourceId != 0) {
                bgView.setBackgroundResource(typedValue.resourceId)
            } else {
                bgView.setBackgroundColor(typedValue.data)
            }
        }
    }

    /**
     * Helper to set corner radius programmatically.
     */
    fun setMcCardCornerRadius(radiusPx: Float) {
        this.radius = radiusPx
    }

    /**
     * Helper to set elevation programmatically.
     */
    fun setMcCardElevation(elevationPx: Float) {
        this.cardElevation = elevationPx
    }

    /**
     * Helper to set stroke programmatically.
     */
    fun setMcCardStroke(widthPx: Int, color: Int) {
        this.strokeWidth = widthPx
        this.strokeColor = color
    }
}