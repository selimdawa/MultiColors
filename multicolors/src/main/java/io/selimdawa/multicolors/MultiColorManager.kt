package io.selimdawa.multicolors

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.R as AndroidR
import androidx.appcompat.R as AppCompatR
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Looper
import android.os.MessageQueue
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.core.view.isNotEmpty
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import io.selimdawa.multicolors.databinding.DialogThemeSelectorBinding
import io.selimdawa.multicolors.databinding.ItemThemeBinding
import io.selimdawa.multicolors.databinding.ItemThemeManageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

val Context.multiColorDataStore by preferencesDataStore(name = "multicolor_prefs")

object MultiColorManager {
    private val themeKey = stringPreferencesKey("color_option")
    private val hiddenThemesKey = stringSetPreferencesKey("hidden_themes")
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _currentThemeId = MutableStateFlow("")
    val currentThemeId: StateFlow<String> = _currentThemeId.asStateFlow()

    private const val DEFAULT_THEME_ID = "S_1"
    var isThemeSafeModeEnabled = true

    /**
     * Set of theme IDs that should be excluded from the theme selection list.
     * Developers can use this to hide themes they don't want to offer in their app.
     */
    var excludedThemeIds: Set<String> = emptySet()

    fun init(application: Application) {
        // Read initial value synchronously to avoid race condition on first activity startup
        val savedThemeId = runBlocking {
            application.multiColorDataStore.data.map {
                it[themeKey] ?: DEFAULT_THEME_ID
            }.first()
        }
        _currentThemeId.value = savedThemeId

        preloadThemesIdle(application)

        managerScope.launch {
            application.multiColorDataStore.data.map {
                it[themeKey] ?: DEFAULT_THEME_ID
            }.collectLatest { themeId ->
                if (_currentThemeId.value != themeId) {
                    _currentThemeId.value = themeId
                }
            }
        }

        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {

            override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
                applyTheme(activity)
                ThemeAnimationHelper.prepareTransition(activity)
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                ThemeAnimationHelper.checkAndPerformRevealAnimation(activity)
                (activity as? AppCompatActivity)?.let { appCompatActivity ->
                    val themeAtCreation = _currentThemeId.value
                    appCompatActivity.lifecycleScope.launch {
                        currentThemeId.collectLatest { themeId ->
                            // Only recreate if the theme has actually changed since this activity was created
                            if (themeAtCreation.isNotEmpty() && themeAtCreation != themeId) {
                                if (!activity.isFinishing && !activity.isDestroyed) {
                                    ThemeAnimationHelper.startThemeChangeAnimation(activity)
                                }
                            }
                        }
                    }
                }
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    fun preloadThemesIdle(context: Context) {
        val allThemes = ThemeRegistry.getAllThemes().filter { it.id !in excludedThemeIds }
        val queue = Looper.myQueue()
        var index = 0

        val idleHandler = MessageQueue.IdleHandler {
            if (index < allThemes.size) {
                val theme = allThemes[index]
                getThemeBackground(context, theme)
                index++
                // Return true to keep the handler active for the next theme
                index < allThemes.size
            } else {
                // All themes preloaded
                false
            }
        }
        queue.addIdleHandler(idleHandler)
    }

    fun applyTheme(context: Context) {
        var themeId = _currentThemeId.value.ifEmpty {
            runBlocking {
                context.multiColorDataStore.data.map {
                    it[themeKey] ?: DEFAULT_THEME_ID
                }.first()
            }
        }

        // If the current theme is excluded by the developer, fallback to default
        if (themeId in excludedThemeIds && themeId != DEFAULT_THEME_ID) {
            themeId = DEFAULT_THEME_ID
        }

        if (isThemeSafeModeEnabled) {
            try {
                performApplyTheme(context, themeId)
            } catch (e: Exception) {
                // Safe Mode: Fallback to default theme
                _currentThemeId.value = DEFAULT_THEME_ID
                context.setTheme(R.style.MC_Base_Theme)

                // Persist the fallback theme to prevent future crashes
                managerScope.launch {
                    context.multiColorDataStore.edit { prefs ->
                        prefs[themeKey] = DEFAULT_THEME_ID
                    }
                }
            }
        } else {
            performApplyTheme(context, themeId)
        }
    }

    private fun performApplyTheme(context: Context, themeId: String) {
        val theme = ThemeRegistry.getTheme(themeId)
        val styleRes = theme.styleRes
        if (styleRes != null) {
            context.setTheme(styleRes)
        } else {
            context.setTheme(R.style.MC_Base_Theme)
        }
    }

    fun showThemeDialog(activity: AppCompatActivity) {
        val dialogBinding = DialogThemeSelectorBinding.inflate(activity.layoutInflater)
        val dialog = AlertDialog.Builder(activity).setView(dialogBinding.root).create()

        dialog.window?.setBackgroundDrawableResource(AndroidR.color.transparent)
        dialog.show()

        val width = (activity.resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val allThemes = ThemeRegistry.getAllThemes().filter { it.id !in excludedThemeIds }

        // Load hidden themes from DataStore
        val hiddenIds = runBlocking {
            activity.multiColorDataStore.data.map {
                it[hiddenThemesKey] ?: emptySet()
            }.first()
        }

        val themesToShow = allThemes.filter { it.id !in hiddenIds }

        val currentThemeId = _currentThemeId.value
        themesToShow.forEach { theme ->
            val itemBinding = ItemThemeBinding.inflate(activity.layoutInflater)
            itemBinding.themeNameText.text = activity.getString(theme.nameRes)
            itemBinding.themeColorView.background = getThemeBackground(activity, theme)

            if (theme.id.equals(currentThemeId, ignoreCase = true)) {
                itemBinding.rootCard.strokeWidth = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 1f, activity.resources.displayMetrics
                ).toInt()
                val typedValue = TypedValue()
                if (activity.theme.resolveAttribute(
                        AppCompatR.attr.colorError, typedValue, true
                    )
                ) {
                    itemBinding.rootCard.strokeColor = typedValue.data
                }
            } else {
                itemBinding.rootCard.strokeWidth = 0
            }

            itemBinding.root.setOnClickListener { view ->
                if (!ThemeAnimationHelper.canPerformAction()) return@setOnClickListener
                
                val newThemeId = theme.id
                val currentId = _currentThemeId.value
                
                // 1. Prevent double clicking if it is the currently selected theme
                if (newThemeId.equals(currentId, ignoreCase = true)) {
                    dialog.dismiss()
                    return@setOnClickListener
                }

                val location = IntArray(2)
                view.getLocationInWindow(location)
                ThemeAnimationHelper.animationStartX = location[0] + view.width / 2
                ThemeAnimationHelper.animationStartY = location[1] + view.height / 2
                
                ThemeAnimationHelper.captureScreenshot(activity) {
                    ThemeAnimationHelper.activeAnimationSource = ThemeAnimationHelper.AnimationSource.THEME_CHANGE
                    _currentThemeId.value = newThemeId
                    managerScope.launch {
                        activity.multiColorDataStore.edit { prefs -> prefs[themeKey] = newThemeId }
                    }
                }
                dialog.dismiss()
            }

            val targetFlexbox = when {
                theme.colors.size == 3 || theme.id.startsWith("G3_") -> dialogBinding.gradient3Flexbox
                theme.id.startsWith("G2_") || (theme.colors.size == 2 && theme.colors[0] != theme.colors[1]) -> dialogBinding.gradient2Flexbox
                else -> dialogBinding.solidFlexbox
            }
            targetFlexbox.addView(itemBinding.root)
        }

        // Hide empty categories
        dialogBinding.tvSolid.visibility =
            if (dialogBinding.solidFlexbox.isNotEmpty()) View.VISIBLE else View.GONE
        dialogBinding.tvGradient2.visibility =
            if (dialogBinding.gradient2Flexbox.isNotEmpty()) View.VISIBLE else View.GONE
        dialogBinding.tvGradient3.visibility =
            if (dialogBinding.gradient3Flexbox.isNotEmpty()) View.VISIBLE else View.GONE

        dialogBinding.btnEditThemes.setOnClickListener {
            dialog.dismiss()
            showManageThemesDialog(activity)
        }
    }

    private fun showManageThemesDialog(activity: AppCompatActivity) {
        val allThemes = ThemeRegistry.getAllThemes().filter { it.id !in excludedThemeIds }
        val currentThemeId = _currentThemeId.value

        val dialogBinding = DialogThemeSelectorBinding.inflate(activity.layoutInflater)
        dialogBinding.dialogTitle.text = activity.getString(R.string.mc_manage_themes)
        dialogBinding.btnEditThemes.visibility = View.GONE
        dialogBinding.btnBack.visibility = View.VISIBLE

        val dialog = AlertDialog.Builder(activity).setView(dialogBinding.root).create()
        dialog.window?.setBackgroundDrawableResource(AndroidR.color.transparent)
        dialog.show()

        dialogBinding.btnBack.setOnClickListener {
            showThemeDialog(activity)
            dialog.dismiss()
        }

        val width = (activity.resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        // Optimized approach: Create views once, then update states
        val itemViews = mutableMapOf<String, ItemThemeManageBinding>()

        allThemes.forEach { theme ->
            val itemBinding = ItemThemeManageBinding.inflate(activity.layoutInflater)
            itemBinding.themeNameText.text = activity.getString(theme.nameRes)
            itemBinding.themeColorView.background = getThemeBackground(activity, theme)
            itemViews[theme.id] = itemBinding

            if (theme.id.equals(currentThemeId, ignoreCase = true)) {
                itemBinding.rootCard.strokeWidth = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 1f, activity.resources.displayMetrics
                ).toInt()
                val typedValue = TypedValue()
                if (activity.theme.resolveAttribute(AppCompatR.attr.colorError, typedValue, true)) {
                    itemBinding.rootCard.strokeColor = typedValue.data
                }
            }

            val root = itemBinding.root
            val targetFlexbox = when {
                theme.colors.size == 3 || theme.id.startsWith("G3_") -> dialogBinding.gradient3Flexbox
                theme.id.startsWith("G2_") || (theme.colors.size == 2 && theme.colors[0] != theme.colors[1]) -> dialogBinding.gradient2Flexbox
                else -> dialogBinding.solidFlexbox
            }
            targetFlexbox.addView(root)

            itemBinding.themeClickArea.setOnClickListener {
                managerScope.launch {
                    activity.multiColorDataStore.edit { p ->
                        val currentHidden = p[hiddenThemesKey]?.toMutableSet() ?: mutableSetOf()
                        if (currentHidden.contains(theme.id)) {
                            currentHidden.remove(theme.id)
                        } else {
                            currentHidden.add(theme.id)
                        }
                        p[hiddenThemesKey] = currentHidden
                    }
                }
            }
        }

        activity.lifecycleScope.launch {
            activity.multiColorDataStore.data.collectLatest { prefs ->
                val hiddenIds = prefs[hiddenThemesKey] ?: emptySet()

                allThemes.forEach { theme ->
                    val itemBinding = itemViews[theme.id] ?: return@forEach
                    val isVisible = !hiddenIds.contains(theme.id)

                    // Update ONLY the state, extremely fast!
                    itemBinding.statusIcon.setImageResource(
                        if (isVisible) AndroidR.drawable.ic_delete
                        else AndroidR.drawable.ic_input_add
                    )
                    itemBinding.statusIcon.setColorFilter(if (isVisible) Color.RED else Color.GREEN)
                }

                // Update headers visibility once
                dialogBinding.tvSolid.visibility =
                    if (dialogBinding.solidFlexbox.isNotEmpty()) View.VISIBLE else View.GONE
                dialogBinding.tvGradient2.visibility =
                    if (dialogBinding.gradient2Flexbox.isNotEmpty()) View.VISIBLE else View.GONE
                dialogBinding.tvGradient3.visibility =
                    if (dialogBinding.gradient3Flexbox.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    fun getThemeBackground(context: Context, theme: MultiColorTheme): Drawable {
        val themeId = theme.id
        val attrId = R.attr.mc_bg
        val isNightMode =
            (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        MultiColorCache.getDrawable(themeId, attrId, isNightMode)?.let { return it.mutate() }

        val styleRes = theme.styleRes
        val drawable = when {
            theme.colors.isNotEmpty() -> {
                GradientDrawable(theme.orientation, theme.colors.toIntArray())
            }

            styleRes != null -> {
                if (theme.id.startsWith("G3_")) {
                    val typedValue = TypedValue()
                    val c = context.resources.newTheme().apply { applyStyle(styleRes, true) }

                    val colors = IntArray(3)
                    val attrs = intArrayOf(R.attr.mc_track, R.attr.mc_center, R.attr.mc_tick)

                    attrs.forEachIndexed { i, attr ->
                        c.resolveAttribute(attr, typedValue, true)
                        colors[i] = if (typedValue.resourceId != 0) ResourcesCompat.getColor(
                            context.resources, typedValue.resourceId, c
                        ) else typedValue.data
                    }
                    GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors)
                } else {
                    val customTheme = context.resources.newTheme()
                    customTheme.applyStyle(styleRes, true)
                    resolveThemeDrawable(context, customTheme, attrId)
                }
            }

            else -> {
                ResourcesCompat.getColor(
                    context.resources, R.color.mc_fallback_color, context.theme
                ).toDrawable()
            }
        }

        MultiColorCache.putDrawable(themeId, attrId, drawable, isNightMode)
        return drawable
    }

    private fun resolveThemeDrawable(
        context: Context, theme: Resources.Theme, attrToResolve: Int
    ): Drawable {
        val typedValue = TypedValue()
        val attrIds = intArrayOf(
            attrToResolve, AndroidR.attr.background, AndroidR.attr.colorBackground
        )

        var resolved = false
        for (attrId in attrIds) {
            if (attrId != 0 && theme.resolveAttribute(attrId, typedValue, true)) {
                resolved = true
                break
            }
        }

        return if (resolved && typedValue.resourceId != 0) {
            ResourcesCompat.getDrawable(context.resources, typedValue.resourceId, theme)!!
        } else if (resolved) {
            typedValue.data.toDrawable()
        } else {
            ResourcesCompat.getColor(context.resources, R.color.mc_fallback_color, theme)
                .toDrawable()
        }
    }

    fun getCurrentTheme(context: Context): MultiColorTheme {
        val id = currentThemeId.value.ifEmpty {
            runBlocking {
                context.multiColorDataStore.data.map {
                    it[themeKey] ?: DEFAULT_THEME_ID
                }.first()
            }
        }
        return ThemeRegistry.getTheme(id)
    }

    private val themeColorsCache = mutableMapOf<String, IntArray>()

    fun getThemeColors(context: Context, theme: MultiColorTheme): IntArray {
        val themeId = theme.id
        val isNightMode =
            (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val cacheKey = "${themeId}_${if (isNightMode) "N" else "D"}"

        themeColorsCache[cacheKey]?.let { return it }

        val styleRes = theme.styleRes
        val colors = if (theme.colors.isNotEmpty()) {
            theme.colors.toIntArray()
        } else if (styleRes != null) {
            val typedValue = TypedValue()
            val themeObj = context.resources.newTheme().apply { applyStyle(styleRes, true) }

            fun resolveColor(attr: Int): Int? {
                return if (themeObj.resolveAttribute(attr, typedValue, true)) {
                    if (typedValue.resourceId != 0) {
                        ResourcesCompat.getColor(context.resources, typedValue.resourceId, themeObj)
                    } else {
                        typedValue.data
                    }
                } else null
            }

            val track = resolveColor(R.attr.mc_track)
            val center = resolveColor(R.attr.mc_center)
            val tick = resolveColor(R.attr.mc_tick)

            if (track != null && tick != null) {
                if (track == tick) {
                    intArrayOf(track, track)
                } else {
                    if (center != null && center != track && center != tick) {
                        intArrayOf(track, center, tick)
                    } else {
                        intArrayOf(track, tick)
                    }
                }
            } else {
                // Fallback to primary/accent
                val primary = resolveColor(AppCompatR.attr.colorPrimary)
                val accent = resolveColor(AppCompatR.attr.colorAccent)
                if (primary != null && accent != null) {
                    intArrayOf(primary, accent)
                } else {
                    null
                }
            }
        } else null

        val finalColors = colors ?: intArrayOf(
            "#FF0000".toColorInt(),
            "#FF7F00".toColorInt(),
            "#FFFF00".toColorInt(),
            "#00FF00".toColorInt(),
            "#0000FF".toColorInt(),
            "#4B0082".toColorInt(),
            "#8B00FF".toColorInt()
        )

        themeColorsCache[cacheKey] = finalColors
        return finalColors
    }
}
