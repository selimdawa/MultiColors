package io.selimdawa.multicolors

import android.graphics.drawable.GradientDrawable
import androidx.annotation.StyleRes

/**
 * Represents a theme in the MultiColors library.
 */
data class MultiColorTheme(
    val id: String,
    val nameRes: Int,
    @get:StyleRes val styleRes: Int? = null,
    val colors: List<Int> = emptyList(),
    val orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.TL_BR
)