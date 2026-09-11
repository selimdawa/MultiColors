package io.selimdawa.multicolors

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.google.android.material.card.MaterialCardView

/**
 * A custom CardView that automatically applies the theme's [mc_bg] background.
 * It encapsulates a background View to support both solid colors and gradients
 * while maintaining MaterialCardView's shape and elevation.
 */
class MultiColorCardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = com.google.android.material.R.attr.materialCardViewStyle
) : MaterialCardView(context, attrs, defStyleAttr) {

    init {
        // Apply default styling consistent with the requested usage
        radius = context.dpToPx(24f)
        cardElevation = 0f
        strokeWidth = 0
        preventCornerOverlap = true

        // Set card background to transparent as we'll use an internal view for mc_bg
        setCardBackgroundColor(Color.TRANSPARENT)

        val bgView = View(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            
            val typedValue = TypedValue()
            if (context.theme.resolveAttribute(R.attr.mc_bg, typedValue, true)) {
                if (typedValue.resourceId != 0) {
                    setBackgroundResource(typedValue.resourceId)
                } else {
                    setBackgroundColor(typedValue.data)
                }
            }
        }

        // Add the background view at index 0 to stay behind any content added in XML
        addView(bgView, 0)
    }
}
