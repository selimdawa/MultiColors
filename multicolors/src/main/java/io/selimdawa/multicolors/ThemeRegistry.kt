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
            MultiColorTheme.Xml("GRADUAL_SEVEN", "Gradual 7", R.style.Theme_MC_Gradient_Seven)
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