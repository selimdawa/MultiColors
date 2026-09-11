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

    private var customBackgroundRes: Int = 0
    private var customBackgroundColor: Int? = null

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

            customBackgroundRes = getResourceId(R.styleable.MultiColorCardView_mc_card_background, 0)
            if (customBackgroundRes == 0 && hasValue(R.styleable.MultiColorCardView_mc_card_background)) {
                customBackgroundColor =
                    getColor(R.styleable.MultiColorCardView_mc_card_background, Color.TRANSPARENT)
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
        if (customBackgroundRes != 0) {
            bgView.setBackgroundResource(customBackgroundRes)
        } else if (customBackgroundColor != null) {
            bgView.setBackgroundColor(customBackgroundColor!!)
        } else {
            val typedValue = TypedValue()
            if (context.theme.resolveAttribute(R.attr.mc_bg, typedValue, true)) {
                if (typedValue.resourceId != 0) {
                    bgView.setBackgroundResource(typedValue.resourceId)
                } else {
                    bgView.setBackgroundColor(typedValue.data)
                }
            } else {
                // Fallback to library default if theme attribute mc_bg is not found
                bgView.setBackgroundResource(R.color.mc_basic)
            }
        }
    }

    /**
     * Set a custom background color for the card.
     */
    fun setMcCardBackground(color: Int) {
        customBackgroundColor = color
        customBackgroundRes = 0
        updateThemeBackground()
    }

    /**
     * Set a custom background resource for the card.
     */
    fun setMcCardBackgroundResource(resId: Int) {
        customBackgroundRes = resId
        customBackgroundColor = null
        updateThemeBackground()
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