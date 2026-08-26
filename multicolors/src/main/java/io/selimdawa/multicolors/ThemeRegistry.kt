package io.selimdawa.multicolors

import android.graphics.Color

object ThemeRegistry {
    private val themes = mutableMapOf<String, MultiColorTheme>()

    init {
        // Register default themes
        registerDefaultThemes()
    }

    private fun registerDefaultThemes() {
        val defaultThemes = listOf(
            // Solids
            MultiColorTheme("S_1", R.string.mc_theme_s1, R.style.Theme_MC_S1),
            MultiColorTheme("S_2", R.string.mc_theme_s2, R.style.Theme_MC_S2),
            MultiColorTheme("S_3", R.string.mc_theme_s3, R.style.Theme_MC_S3),
            MultiColorTheme("S_4", R.string.mc_theme_s4, R.style.Theme_MC_S4),
            MultiColorTheme("S_5", R.string.mc_theme_s5, R.style.Theme_MC_S5),
            MultiColorTheme("S_6", R.string.mc_theme_s6, R.style.Theme_MC_S6),
            MultiColorTheme("S_7", R.string.mc_theme_s7, R.style.Theme_MC_S7, listOf(Color.parseColor("#607D8B"), Color.parseColor("#607D8B"))),
            MultiColorTheme("S_8", R.string.mc_theme_s8, R.style.Theme_MC_S8, listOf(Color.parseColor("#5C6BC0"), Color.parseColor("#5C6BC0"))),
            MultiColorTheme("S_9", R.string.mc_theme_s9, R.style.Theme_MC_S9, listOf(Color.parseColor("#008080"), Color.parseColor("#008080"))),
            MultiColorTheme("S_10", R.string.mc_theme_s10, R.style.Theme_MC_S10, listOf(Color.parseColor("#800080"), Color.parseColor("#800080"))),

            // 2-Color Gradients
            MultiColorTheme("G2_1", R.string.mc_theme_g2_1, R.style.Theme_MC_G2_1),
            MultiColorTheme("G2_2", R.string.mc_theme_g2_2, R.style.Theme_MC_G2_2),
            MultiColorTheme("G2_3", R.string.mc_theme_g2_3, R.style.Theme_MC_G2_3),
            MultiColorTheme("G2_4", R.string.mc_theme_g2_4, R.style.Theme_MC_G2_4),
            MultiColorTheme("G2_5", R.string.mc_theme_g2_5, R.style.Theme_MC_G2_5),
            MultiColorTheme("G2_6", R.string.mc_theme_g2_6, R.style.Theme_MC_G2_6),
            MultiColorTheme("G2_7", R.string.mc_theme_g2_7, R.style.Theme_MC_G2_7),
            MultiColorTheme("G2_8", R.string.mc_theme_g2_8, R.style.Theme_MC_G2_8),
            MultiColorTheme("G2_9", R.string.mc_theme_g2_9, R.style.Theme_MC_G2_9),
            MultiColorTheme("G2_10", R.string.mc_theme_g2_10, R.style.Theme_MC_G2_10),
            MultiColorTheme("G2_11", R.string.mc_theme_g2_11, R.style.Theme_MC_G2_11),
            MultiColorTheme("G2_12", R.string.mc_theme_g2_12, R.style.Theme_MC_G2_12),
            MultiColorTheme("G2_13", R.string.mc_theme_g2_13, R.style.Theme_MC_G2_13),
            MultiColorTheme("G2_14", R.string.mc_theme_g2_14, R.style.Theme_MC_G2_14),
            MultiColorTheme("G2_15", R.string.mc_theme_g2_15, R.style.Theme_MC_G2_15),
            MultiColorTheme("G2_16", R.string.mc_theme_g2_16, R.style.Theme_MC_G2_16),
            MultiColorTheme("G2_17", R.string.mc_theme_g2_17, R.style.Theme_MC_G2_17),
            MultiColorTheme("G2_18", R.string.mc_theme_g2_18, R.style.Theme_MC_G2_18),
            MultiColorTheme("G2_19", R.string.mc_theme_g2_19, R.style.Theme_MC_G2_19),

            // 3-Color Gradients
            MultiColorTheme("G3_1", R.string.mc_theme_g3_1, R.style.Theme_MC_G3_1),
            MultiColorTheme("G3_2", R.string.mc_theme_g3_2, R.style.Theme_MC_G3_2),
            MultiColorTheme("G3_3", R.string.mc_theme_g3_3, R.style.Theme_MC_G3_3),
            MultiColorTheme("G3_4", R.string.mc_theme_g3_4, R.style.Theme_MC_G3_4),
            MultiColorTheme("G3_5", R.string.mc_theme_g3_5, R.style.Theme_MC_G3_5),
            MultiColorTheme("G3_6", R.string.mc_theme_g3_6, R.style.Theme_MC_G3_6),
            MultiColorTheme("G3_7", R.string.mc_theme_g3_7, R.style.Theme_MC_G3_7),
            MultiColorTheme("G3_8", R.string.mc_theme_g3_8, R.style.Theme_MC_G3_8),
            MultiColorTheme("G3_9", R.string.mc_theme_g3_9, R.style.Theme_MC_G3_9),
            MultiColorTheme("G3_10", R.string.mc_theme_g3_10, R.style.Theme_MC_G3_10)
        )
        defaultThemes.forEach { register(it) }
    }

    fun register(theme: MultiColorTheme) {
        themes[theme.id] = theme
    }

    fun getTheme(id: String): MultiColorTheme {
        if (id.startsWith("CUSTOM_")) {
            try {
                val colorHex = id.substringAfter("CUSTOM_")
                val color = Color.parseColor("#$colorHex")
                return MultiColorTheme(id, R.string.mc_theme_custom, colors = listOf(color, color))
            } catch (e: Exception) {
                // Fallback
            }
        }
        return themes[id] ?: themes.values.first()
    }

    fun getAllThemes(): List<MultiColorTheme> {
        return themes.values.toList()
    }
}