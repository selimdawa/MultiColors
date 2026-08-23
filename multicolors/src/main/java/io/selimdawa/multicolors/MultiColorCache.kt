package io.selimdawa.multicolors

import android.graphics.drawable.Drawable
import android.util.LruCache

/**
 * A centralized cache for resolved colors and drawables to improve performance.
 */
@Suppress("unused")
object MultiColorCache {
    // Cache for Drawables (e.g., backgrounds, gradients)
    private val drawableCache = LruCache<String, Drawable>(128)

    // Cache for resolved color integers (e.g., mc_track, mc_tick)
    private val colorCache = LruCache<String, Int>(256)

    fun getDrawable(themeId: String, attrId: Int, isNightMode: Boolean): Drawable? {
        val mode = if (isNightMode) "N" else "D"
        return drawableCache.get("${themeId}_${attrId}_$mode")
    }

    fun putDrawable(themeId: String, attrId: Int, drawable: Drawable, isNightMode: Boolean) {
        val mode = if (isNightMode) "N" else "D"
        drawableCache.put("${themeId}_${attrId}_$mode", drawable)
    }

    fun getColor(themeId: String, attrId: Int, isNightMode: Boolean): Int? {
        val mode = if (isNightMode) "N" else "D"
        return colorCache.get("${themeId}_${attrId}_$mode")
    }

    fun putColor(themeId: String, attrId: Int, color: Int, isNightMode: Boolean) {
        val mode = if (isNightMode) "N" else "D"
        colorCache.put("${themeId}_${attrId}_$mode", color)
    }

    /**
     * Clears all cached items. Use this if memory needs to be reclaimed.
     */
    fun clear() {
        drawableCache.evictAll()
        colorCache.evictAll()
    }
}