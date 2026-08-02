package io.selimdawa.multicolors

import androidx.annotation.StyleRes

/**
 * Represents a theme in the MultiColors library.
 */
sealed class MultiColorTheme {
    abstract val id: String
    abstract val name: String

    /**
     * A theme defined in XML styles (same as library defaults).
     * This is the most powerful type as it supports gradients and native Android styles.
     */
    data class Xml(
        override val id: String,
        override val name: String,
        @StyleRes val styleRes: Int
    ) : MultiColorTheme()
}