package io.selimdawa.multicolors

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
            MultiColorTheme("FOUR", R.string.mc_theme_four, R.style.Theme_MC_Four),
            MultiColorTheme("FIVE", R.string.mc_theme_five, R.style.Theme_MC_Five),
            MultiColorTheme("SIX", R.string.mc_theme_six, R.style.Theme_MC_Six),
            MultiColorTheme("SEVEN", R.string.mc_theme_seven, R.style.Theme_MC_Seven),
            MultiColorTheme("EIGHT", R.string.mc_theme_eight, R.style.Theme_MC_Eight),
            MultiColorTheme("NINE", R.string.mc_theme_nine, R.style.Theme_MC_Nine),
            MultiColorTheme("GRADUAL_ONE", R.string.mc_theme_gradient_one, R.style.Theme_MC_Gradient_One),
            MultiColorTheme("GRADUAL_TWO", R.string.mc_theme_gradient_two, R.style.Theme_MC_Gradient_Two),
            MultiColorTheme("GRADUAL_THREE", R.string.mc_theme_gradient_three, R.style.Theme_MC_Gradient_Three),
            MultiColorTheme("GRADUAL_FOUR", R.string.mc_theme_gradient_four, R.style.Theme_MC_Gradient_Four),
            MultiColorTheme("GRADUAL_FIVE", R.string.mc_theme_gradient_five, R.style.Theme_MC_Gradient_Five),
            MultiColorTheme("GRADUAL_SIX", R.string.mc_theme_gradient_six, R.style.Theme_MC_Gradient_Six),
            MultiColorTheme("GRADUAL_SEVEN", R.string.mc_theme_gradient_seven, R.style.Theme_MC_Gradient_Seven),

            // Additional Solid Colors
            MultiColorTheme("SOLID_BLACK", R.string.mc_theme_black, R.style.Theme_MC_Black),
            MultiColorTheme("SOLID_WHITE", R.string.mc_theme_white, R.style.Theme_MC_White),
            MultiColorTheme("SOLID_CYAN", R.string.mc_theme_cyan, R.style.Theme_MC_Cyan),
            MultiColorTheme("SOLID_MAGENTA", R.string.mc_theme_magenta, R.style.Theme_MC_Magenta),
            MultiColorTheme("SOLID_YELLOW", R.string.mc_theme_yellow, R.style.Theme_MC_Yellow),
            MultiColorTheme("SOLID_TEAL", R.string.mc_theme_teal, R.style.Theme_MC_Teal),
            MultiColorTheme("SOLID_PURPLE", R.string.mc_theme_purple, R.style.Theme_MC_Purple),
            MultiColorTheme("SOLID_NAVY", R.string.mc_theme_navy, R.style.Theme_MC_Navy),

            // 2-Color Gradients
            MultiColorTheme("G2_1", R.string.mc_theme_sunset, R.style.Theme_MC_G2_1),
            MultiColorTheme("G2_2", R.string.mc_theme_sea_blue, R.style.Theme_MC_G2_2),
            MultiColorTheme("G2_3", R.string.mc_theme_mango, R.style.Theme_MC_G2_3),
            MultiColorTheme("G2_4", R.string.mc_theme_purple_love, R.style.Theme_MC_G2_4),
            MultiColorTheme("G2_5", R.string.mc_theme_peach, R.style.Theme_MC_G2_5),
            MultiColorTheme("G2_6", R.string.mc_theme_light_purple, R.style.Theme_MC_G2_6),
            MultiColorTheme("G2_7", R.string.mc_theme_deep_space, R.style.Theme_MC_G2_7),
            MultiColorTheme("G2_8", R.string.mc_theme_cristal, R.style.Theme_MC_G2_8),
            MultiColorTheme("G2_9", R.string.mc_theme_emerald, R.style.Theme_MC_G2_9),
            MultiColorTheme("G2_10", R.string.mc_theme_relief, R.style.Theme_MC_G2_10),
            MultiColorTheme("G2_11", R.string.mc_theme_morpheus, R.style.Theme_MC_G2_11),
            MultiColorTheme("G2_12", R.string.mc_theme_shadow, R.style.Theme_MC_G2_12),
            MultiColorTheme("G2_13", R.string.mc_theme_copper, R.style.Theme_MC_G2_13),
            MultiColorTheme("G2_14", R.string.mc_theme_aura, R.style.Theme_MC_G2_14),
            MultiColorTheme("G2_15", R.string.mc_theme_vast, R.style.Theme_MC_G2_15),
            MultiColorTheme("G2_16", R.string.mc_theme_horizon, R.style.Theme_MC_G2_16),
            MultiColorTheme("G2_17", R.string.mc_theme_grit, R.style.Theme_MC_G2_17),
            MultiColorTheme("G2_18", R.string.mc_theme_blood, R.style.Theme_MC_G2_18),
            MultiColorTheme("G2_19", R.string.mc_theme_lime, R.style.Theme_MC_G2_19),
            MultiColorTheme("G2_20", R.string.mc_theme_frost, R.style.Theme_MC_G2_20),
            MultiColorTheme("G2_21", R.string.mc_theme_lush, R.style.Theme_MC_G2_21),
            MultiColorTheme("G2_22", R.string.mc_theme_aqua, R.style.Theme_MC_G2_22),
            MultiColorTheme("G2_23", R.string.mc_theme_youtube, R.style.Theme_MC_G2_23),
            MultiColorTheme("G2_24", R.string.mc_theme_cool_sky, R.style.Theme_MC_G2_24),
            MultiColorTheme("G2_25", R.string.mc_theme_mighty_blue, R.style.Theme_MC_G2_25),
            MultiColorTheme("G2_26", R.string.mc_theme_evening, R.style.Theme_MC_G2_26),
            MultiColorTheme("G2_27", R.string.mc_theme_kyoto, R.style.Theme_MC_G2_27),
            MultiColorTheme("G2_28", R.string.mc_theme_kimoby, R.style.Theme_MC_G2_28),

            // 3-Color Gradients
            MultiColorTheme("G3_1", R.string.mc_theme_rainbow_3, R.style.Theme_MC_G3_1),
            MultiColorTheme("G3_2", R.string.mc_theme_neon_night, R.style.Theme_MC_G3_2),
            MultiColorTheme("G3_3", R.string.mc_theme_ocean_deep, R.style.Theme_MC_G3_3),
            MultiColorTheme("G3_4", R.string.mc_theme_fire, R.style.Theme_MC_G3_4),
            MultiColorTheme("G3_5", R.string.mc_theme_sky, R.style.Theme_MC_G3_5),
            MultiColorTheme("G3_6", R.string.mc_theme_pastel, R.style.Theme_MC_G3_6),
            MultiColorTheme("G3_7", R.string.mc_theme_lemonade, R.style.Theme_MC_G3_7),
            MultiColorTheme("G3_8", R.string.mc_theme_winter_3, R.style.Theme_MC_G3_8),
            MultiColorTheme("G3_9", R.string.mc_theme_galaxy, R.style.Theme_MC_G3_9),
            MultiColorTheme("G3_10", R.string.mc_theme_spectrum, R.style.Theme_MC_G3_10),
            MultiColorTheme("G3_11", R.string.mc_theme_candy, R.style.Theme_MC_G3_11),
            MultiColorTheme("G3_12", R.string.mc_theme_winter, R.style.Theme_MC_G3_12),
            MultiColorTheme("G3_13", R.string.mc_theme_deep_sea, R.style.Theme_MC_G3_13)
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
                val color = android.graphics.Color.parseColor("#$colorHex")
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