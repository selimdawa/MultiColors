package io.selimdawa.multicolors

sealed class MultiColorTheme {
    abstract val id: String
    abstract val name: String
    
    abstract val mc_bg: Int
    abstract val mc_track: Int
    abstract val mc_tick: Int

    data class Xml(
        override val id: String,
        override val name: String,
        val styleRes: Int
    ) : MultiColorTheme() {
        override val mc_bg: Int = 0
        override val mc_track: Int = 0
        override val mc_tick: Int = 0
    }

    data class Dynamic(
        override val id: String,
        override val name: String,
        val solidColor: Int
    ) : MultiColorTheme() {
        override val mc_bg: Int get() = solidColor
        override val mc_track: Int get() = solidColor
        override val mc_tick: Int get() = solidColor
    }
}
