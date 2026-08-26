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
            MultiColorTheme("ONE", R.string.mc_theme_one, R.style.Theme_MC_One),
            MultiColorTheme("TWO", R.string.mc_theme_two, R.style.Theme_MC_Two),
            MultiColorTheme("THREE", R.string.mc_theme_three, R.style.Theme_MC_Three),
            MultiColorTheme("FIVE", R.string.mc_theme_five, R.style.Theme_MC_Five),
            MultiColorTheme("SEVEN", R.string.mc_theme_seven, R.style.Theme_MC_Seven),
            MultiColorTheme("NINE", R.string.mc_theme_nine, R.style.Theme_MC_Nine),
            MultiColorTheme("GRADUAL_ONE", R.string.mc_theme_gradient_one, R.style.Theme_MC_Gradient_One),
            MultiColorTheme("GRADUAL_FOUR", R.string.mc_theme_gradient_four, R.style.Theme_MC_Gradient_Four),
            MultiColorTheme("GRADUAL_FIVE", R.string.mc_theme_gradient_five, R.style.Theme_MC_Gradient_Five),
            MultiColorTheme("GRADUAL_SIX", R.string.mc_theme_gradient_six, R.style.Theme_MC_Gradient_Six),
            MultiColorTheme("GRADUAL_SEVEN", R.string.mc_theme_gradient_seven, R.style.Theme_MC_Gradient_Seven),

            // Additional Solid Colors
            MultiColorTheme("SOLID_SLATE", R.string.mc_theme_slate_solid, R.style.Theme_MC_Slate, listOf(Color.parseColor("#78909C"), Color.parseColor("#78909C"))),
            MultiColorTheme("SOLID_INDIGO", R.string.mc_theme_indigo_solid, R.style.Theme_MC_Indigo, listOf(Color.parseColor("#3F51B5"), Color.parseColor("#3F51B5"))),
            MultiColorTheme("SOLID_TEAL", R.string.mc_theme_teal, R.style.Theme_MC_Teal, listOf(Color.parseColor("#008080"), Color.parseColor("#008080"))),
            MultiColorTheme("SOLID_PURPLE", R.string.mc_theme_purple, R.style.Theme_MC_Purple, listOf(Color.parseColor("#800080"), Color.parseColor("#800080"))),

            // 2-Color Gradients
            MultiColorTheme("G2_2", R.string.mc_theme_sea_blue, R.style.Theme_MC_G2_2),
            MultiColorTheme("G2_8", R.string.mc_theme_cristal, R.style.Theme_MC_G2_8),
            MultiColorTheme("G2_10", R.string.mc_theme_relief, R.style.Theme_MC_G2_10),
            MultiColorTheme("G2_13", R.string.mc_theme_copper, R.style.Theme_MC_G2_13),
            MultiColorTheme("G2_14", R.string.mc_theme_aura, R.style.Theme_MC_G2_14),
            MultiColorTheme("G2_17", R.string.mc_theme_grit, R.style.Theme_MC_G2_17),
            MultiColorTheme("G2_28", R.string.mc_theme_kimoby, R.style.Theme_MC_G2_28),
            MultiColorTheme("G2_29", R.string.mc_theme_kimoby_green, R.style.Theme_MC_G2_29),
            MultiColorTheme("G2_31", R.string.mc_theme_kimoby_purple, R.style.Theme_MC_G2_31),
            MultiColorTheme("G2_33", R.string.mc_theme_slate, R.style.Theme_MC_G2_33),
            MultiColorTheme("G2_35", R.string.mc_theme_clay, R.style.Theme_MC_G2_35),
            MultiColorTheme("G2_37", R.string.mc_theme_dusty_blue, R.style.Theme_MC_G2_37),
            MultiColorTheme("G2_38", R.string.mc_theme_olive, R.style.Theme_MC_G2_38),
            MultiColorTheme("G2_39", R.string.mc_theme_rose, R.style.Theme_MC_G2_39),

            // 3-Color Gradients
            MultiColorTheme("G3_2", R.string.mc_theme_neon, R.style.Theme_MC_G3_2),
            MultiColorTheme("G3_5", R.string.mc_theme_sky, R.style.Theme_MC_G3_5),
            MultiColorTheme("G3_9", R.string.mc_theme_galaxy, R.style.Theme_MC_G3_9),
            MultiColorTheme("G3_10", R.string.mc_theme_galaxy_blue, R.style.Theme_MC_G3_10),
            MultiColorTheme("G3_11", R.string.mc_theme_galaxy_emerald, R.style.Theme_MC_G3_11),

            // Sky Themes (3-Color Gradients)
            MultiColorTheme("SKY_BLUE", R.string.mc_theme_sky_blue, R.style.Theme_MC_Sky_Blue),
            MultiColorTheme("SKY_SUNSET", R.string.mc_theme_sky_sunset, R.style.Theme_MC_Sky_Sunset),
            MultiColorTheme("SKY_GREEN", R.string.mc_theme_sky_green, R.style.Theme_MC_Sky_Green),
            MultiColorTheme("SKY_RED", R.string.mc_theme_sky_red, R.style.Theme_MC_Sky_Red),
            MultiColorTheme("SKY_TEAL", R.string.mc_theme_sky_teal, R.style.Theme_MC_Sky_Teal)
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