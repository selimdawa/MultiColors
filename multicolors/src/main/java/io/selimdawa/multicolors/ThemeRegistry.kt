package io.selimdawa.multicolors

object ThemeRegistry {
    private val themes = mutableMapOf<String, MultiColorTheme>()

    init {
        // Register default themes
        registerDefaultThemes()
    }

    private fun registerDefaultThemes() {
        val defaultThemes = listOf(
            MultiColorTheme.Xml("ONE", "Basic", R.style.Theme_MC_One),
            MultiColorTheme.Xml("TWO", "Blue", R.style.Theme_MC_Two),
            MultiColorTheme.Xml("THREE", "Orange 1", R.style.Theme_MC_Three),
            MultiColorTheme.Xml("FOUR", "Orange 2", R.style.Theme_MC_Four),
            MultiColorTheme.Xml("FIVE", "Fuchsia", R.style.Theme_MC_Five),
            MultiColorTheme.Xml("SIX", "Pink", R.style.Theme_MC_Six),
            MultiColorTheme.Xml("SEVEN", "Brown", R.style.Theme_MC_Seven),
            MultiColorTheme.Xml("EIGHT", "Red", R.style.Theme_MC_Eight),
            MultiColorTheme.Xml("NINE", "Green", R.style.Theme_MC_Nine),
            MultiColorTheme.Xml("GRADUAL_ONE", "Gradual 1", R.style.Theme_MC_Gradient_One),
            MultiColorTheme.Xml("GRADUAL_TWO", "Gradual 2", R.style.Theme_MC_Gradient_Two),
            MultiColorTheme.Xml("GRADUAL_THREE", "Gradual 3", R.style.Theme_MC_Gradient_Three),
            MultiColorTheme.Xml("GRADUAL_FOUR", "Gradual 4", R.style.Theme_MC_Gradient_Four),
            MultiColorTheme.Xml("GRADUAL_FIVE", "Gradual 5", R.style.Theme_MC_Gradient_Five),
            MultiColorTheme.Xml("GRADUAL_SIX", "Gradual 6", R.style.Theme_MC_Gradient_Six),
            MultiColorTheme.Xml("GRADUAL_SEVEN", "Gradual 7", R.style.Theme_MC_Gradient_Seven),

            // Additional Solid Colors
            MultiColorTheme.Xml("SOLID_BLACK", "Pure Black", R.style.Theme_MC_Black),
            MultiColorTheme.Xml("SOLID_WHITE", "Pure White", R.style.Theme_MC_White),
            MultiColorTheme.Xml("SOLID_CYAN", "Cyan", R.style.Theme_MC_Cyan),
            MultiColorTheme.Xml("SOLID_MAGENTA", "Magenta", R.style.Theme_MC_Magenta),
            MultiColorTheme.Xml("SOLID_YELLOW", "Yellow", R.style.Theme_MC_Yellow),
            MultiColorTheme.Xml("SOLID_TEAL", "Teal", R.style.Theme_MC_Teal),
            MultiColorTheme.Xml("SOLID_PURPLE", "Purple", R.style.Theme_MC_Purple),
            MultiColorTheme.Xml("SOLID_NAVY", "Navy", R.style.Theme_MC_Navy),

            // 2-Color Gradients
            MultiColorTheme.Xml("G2_1", "Sunset", R.style.Theme_MC_G2_1),
            MultiColorTheme.Xml("G2_2", "Sea Blue", R.style.Theme_MC_G2_2),
            MultiColorTheme.Xml("G2_3", "Mango", R.style.Theme_MC_G2_3),
            MultiColorTheme.Xml("G2_4", "Purple Love", R.style.Theme_MC_G2_4),
            MultiColorTheme.Xml("G2_5", "Peach", R.style.Theme_MC_G2_5),
            MultiColorTheme.Xml("G2_6", "Light Purple", R.style.Theme_MC_G2_6),
            MultiColorTheme.Xml("G2_7", "Deep Space", R.style.Theme_MC_G2_7),
            MultiColorTheme.Xml("G2_8", "Cristal", R.style.Theme_MC_G2_8),
            MultiColorTheme.Xml("G2_9", "Emerald", R.style.Theme_MC_G2_9),
            MultiColorTheme.Xml("G2_10", "Relief", R.style.Theme_MC_G2_10),
            MultiColorTheme.Xml("G2_11", "Morpheus", R.style.Theme_MC_G2_11),
            MultiColorTheme.Xml("G2_12", "Shadow", R.style.Theme_MC_G2_12),
            MultiColorTheme.Xml("G2_13", "Copper", R.style.Theme_MC_G2_13),
            MultiColorTheme.Xml("G2_14", "Aura", R.style.Theme_MC_G2_14),
            MultiColorTheme.Xml("G2_15", "Vast", R.style.Theme_MC_G2_15),
            MultiColorTheme.Xml("G2_16", "Horizon", R.style.Theme_MC_G2_16),
            MultiColorTheme.Xml("G2_17", "Grit", R.style.Theme_MC_G2_17),
            MultiColorTheme.Xml("G2_18", "Blood", R.style.Theme_MC_G2_18),
            MultiColorTheme.Xml("G2_19", "Lime", R.style.Theme_MC_G2_19),
            MultiColorTheme.Xml("G2_20", "Frost", R.style.Theme_MC_G2_20),
            MultiColorTheme.Xml("G2_21", "Lush", R.style.Theme_MC_G2_21),
            MultiColorTheme.Xml("G2_22", "Aqua", R.style.Theme_MC_G2_22),
            MultiColorTheme.Xml("G2_23", "YouTube", R.style.Theme_MC_G2_23),
            MultiColorTheme.Xml("G2_24", "Cool Sky", R.style.Theme_MC_G2_24),
            MultiColorTheme.Xml("G2_25", "Mighty Blue", R.style.Theme_MC_G2_25),
            MultiColorTheme.Xml("G2_26", "Evening", R.style.Theme_MC_G2_26),
            MultiColorTheme.Xml("G2_27", "Kyoto", R.style.Theme_MC_G2_27),
            MultiColorTheme.Xml("G2_28", "Kimoby", R.style.Theme_MC_G2_28),

            // 3-Color Gradients
            MultiColorTheme.Xml("G3_1", "Rainbow 3", R.style.Theme_MC_G3_1),
            MultiColorTheme.Xml("G3_2", "Neon Night", R.style.Theme_MC_G3_2),
            MultiColorTheme.Xml("G3_3", "Ocean Deep", R.style.Theme_MC_G3_3),
            MultiColorTheme.Xml("G3_4", "Fire", R.style.Theme_MC_G3_4),
            MultiColorTheme.Xml("G3_5", "Sky", R.style.Theme_MC_G3_5),
            MultiColorTheme.Xml("G3_6", "Pastel", R.style.Theme_MC_G3_6),
            MultiColorTheme.Xml("G3_7", "Lemonade", R.style.Theme_MC_G3_7),
            MultiColorTheme.Xml("G3_8", "Winter 3", R.style.Theme_MC_G3_8),
            MultiColorTheme.Xml("G3_9", "Galaxy", R.style.Theme_MC_G3_9),
            MultiColorTheme.Xml("G3_10", "Spectrum", R.style.Theme_MC_G3_10),
            MultiColorTheme.Xml("G3_11", "Candy", R.style.Theme_MC_G3_11),
            MultiColorTheme.Xml("G3_12", "Winter", R.style.Theme_MC_G3_12),
            MultiColorTheme.Xml("G3_13", "Deep Sea", R.style.Theme_MC_G3_13)
        )
        defaultThemes.forEach { register(it) }
    }

    fun register(theme: MultiColorTheme) {
        themes[theme.id] = theme
    }

    fun getTheme(id: String): MultiColorTheme {
        return themes[id] ?: themes.values.first()
    }

    fun getAllThemes(): List<MultiColorTheme> {
        return themes.values.toList()
    }
}