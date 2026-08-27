package io.selimdawa.multicolors

import android.content.Context
import android.util.TypedValue

/**
 * Converts dp to pixels.
 */
internal fun Context.dpToPx(dp: Float): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
)

/**
 * Converts dp to pixels as an integer.
 */
internal fun Context.dpToPxInt(dp: Float): Int = dpToPx(dp).toInt()