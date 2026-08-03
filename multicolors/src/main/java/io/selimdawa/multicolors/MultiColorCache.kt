package io.selimdawa.multicolors

import android.graphics.drawable.Drawable
import android.util.LruCache

/**
 * A centralized cache for resolved colors and drawables to improve performance.
 */
object MultiColorCache {
    // Cache for Drawables (e.g., backgrounds, gradients)
    private val drawableCache = LruCache<String, Drawable>(32)

    // Cache for resolved color integers (e.g., mc_track, mc_tick)
    private val colorCache = LruCache<String, Int>(64)

    fun getDrawable(themeId: String, attrId: Int): Drawable? {
        return drawableCache.get("${themeId}_$attrId")
    }

    fun putDrawable(themeId: String, attrId: Int, drawable: Drawable) {
        drawableCache.put("${themeId}_$attrId", drawable)
    }

    fun getColor(themeId: String, attrId: Int): Int? {
        return colorCache.get("${themeId}_$attrId")
    }

    fun putColor(themeId: String, attrId: Int, color: Int) {
        colorCache.put("${themeId}_$attrId", color)
    }

    /**
     * Clears all cached items. Use this if memory needs to be reclaimed.
     */
    fun clear() {
        drawableCache.evictAll()
        colorCache.evictAll()
    }
}