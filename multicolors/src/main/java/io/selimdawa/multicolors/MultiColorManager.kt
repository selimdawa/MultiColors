package io.selimdawa.multicolors

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Looper
import android.os.MessageQueue
import android.util.TypedValue
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isNotEmpty
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import io.selimdawa.multicolors.databinding.DialogColorPickerBinding
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
    private val mainThemesKey = stringSetPreferencesKey("main_themes")
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _currentThemeId = MutableStateFlow("")
    val currentThemeId: StateFlow<String> = _currentThemeId.asStateFlow()

    private const val DEFAULT_THEME_ID = "ONE"
    var isThemeSafeModeEnabled = true

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
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                (activity as? AppCompatActivity)?.let { appCompatActivity ->
                    val themeAtCreation = _currentThemeId.value
                    appCompatActivity.lifecycleScope.launch {
                        currentThemeId.collectLatest { themeId ->
                            // Only recreate if the theme has actually changed since this activity was created
                            if (themeAtCreation.isNotEmpty() && themeAtCreation != themeId) {
                                ThemeAnimationHelper.startThemeChangeAnimation(activity)
                            }
                        }
                    }
                }
            }

            override fun onActivityStarted(activity: Activity) {
                ThemeAnimationHelper.checkAndPerformRevealAnimation(activity)
            }

            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    fun preloadThemesIdle(context: Context) {
        val allThemes = ThemeRegistry.getAllThemes()
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
        val themeId = _currentThemeId.value.ifEmpty {
            runBlocking {
                context.multiColorDataStore.data.map {
                    it[themeKey] ?: DEFAULT_THEME_ID
                }.first()
            }
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

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val width = (activity.resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val allThemes = ThemeRegistry.getAllThemes()
        val defaultIds = allThemes.filter { !it.id.contains("GRADUAL") }.take(3).map { it.id }
            .toSet() + allThemes.filter { it.id.contains("GRADUAL") }.take(3).map { it.id }.toSet()

        // Load main themes from DataStore
        val savedThemeIds = runBlocking {
            activity.multiColorDataStore.data.map {
                it[mainThemesKey]
            }.first()
        }

        val effectiveIds = savedThemeIds ?: defaultIds
        val themesToShow = allThemes.filter { effectiveIds.contains(it.id) }

        themesToShow.forEach { theme ->
            val itemBinding = ItemThemeBinding.inflate(activity.layoutInflater)
            itemBinding.themeNameText.text = theme.name
            itemBinding.themeColorView.background = getThemeBackground(activity, theme)

            itemBinding.root.setOnClickListener { view ->
                val location = IntArray(2)
                view.getLocationInWindow(location)
                ThemeAnimationHelper.animationStartX = location[0] + view.width / 2
                ThemeAnimationHelper.animationStartY = location[1] + view.height / 2

                dialog.setOnDismissListener {
                    ThemeAnimationHelper.captureScreenshot(activity)
                    val newThemeId = theme.id
                    _currentThemeId.value = newThemeId
                    managerScope.launch {
                        activity.multiColorDataStore.edit { prefs -> prefs[themeKey] = newThemeId }
                    }
                }
                dialog.dismiss()
            }

            val targetFlexbox = when {
                theme.colors.size == 3 || theme.id.startsWith(
                    "G3_"
                ) -> dialogBinding.gradient3Flexbox

                theme.id.contains("GRADUAL") || theme.id.startsWith("G2_") || (theme.colors.size == 2 && theme.colors[0] != theme.colors[1]) -> dialogBinding.gradient2Flexbox
                else -> dialogBinding.solidFlexbox
            }
            targetFlexbox.addView(itemBinding.root)
        }

        // Hide empty categories
        dialogBinding.tvSolid.visibility =
            if (dialogBinding.solidFlexbox.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        dialogBinding.tvGradient2.visibility =
            if (dialogBinding.gradient2Flexbox.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        dialogBinding.tvGradient3.visibility =
            if (dialogBinding.gradient3Flexbox.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE

        dialogBinding.btnEditThemes.setOnClickListener {
            dialog.dismiss()
            showManageThemesDialog(activity)
        }

        dialogBinding.btnCustomTheme.setOnClickListener {
            dialog.dismiss()
            showColorPickerDialog(activity)
        }
    }

    private fun showColorPickerDialog(activity: AppCompatActivity) {
        val dialogBinding = DialogColorPickerBinding.inflate(activity.layoutInflater)
        val dialog = AlertDialog.Builder(activity).setView(dialogBinding.root).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val width = (activity.resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val colorWheel = dialogBinding.colorWheelView
        val preview = dialogBinding.colorPreview
        val hexInput = dialogBinding.hexEditText

        // Initialize with current color if it's custom
        val currentTheme = getCurrentTheme(activity)
        if (currentTheme.id.startsWith("CUSTOM_")) {
            val color = currentTheme.colors[0]
            colorWheel.setColor(color)
            preview.setBackgroundColor(color)
            hexInput.setText(String.format("#%06X", (0xFFFFFF and color)))
        }

        colorWheel.setOnColorChangeListener { color ->
            preview.setBackgroundColor(color)
            hexInput.setText(String.format("#%06X", (0xFFFFFF and color)))
        }

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }

        dialogBinding.btnSave.setOnClickListener {
            val color = colorWheel.getColor()
            val hex = String.format("%06X", (0xFFFFFF and color))
            val newThemeId = "CUSTOM_$hex"

            ThemeAnimationHelper.captureScreenshot(activity)
            _currentThemeId.value = newThemeId
            managerScope.launch {
                activity.multiColorDataStore.edit { prefs -> prefs[themeKey] = newThemeId }
            }
            dialog.dismiss()
        }
    }

    private fun showManageThemesDialog(activity: AppCompatActivity) {
        val allThemes = ThemeRegistry.getAllThemes()
        val defaultIds = allThemes.filter { !it.id.contains("GRADUAL") }.take(3).map { it.id }
            .toSet() + allThemes.filter { it.id.contains("GRADUAL") }.take(3).map { it.id }.toSet()

        val dialogBinding = DialogThemeSelectorBinding.inflate(activity.layoutInflater)
        dialogBinding.dialogTitle.text = activity.getString(R.string.mc_manage_themes)
        dialogBinding.btnEditThemes.visibility = android.view.View.GONE
        dialogBinding.btnBack.visibility = android.view.View.VISIBLE

        val dialog = AlertDialog.Builder(activity).setView(dialogBinding.root).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
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
            itemBinding.themeNameText.text = theme.name
            itemBinding.themeColorView.background = getThemeBackground(activity, theme)
            itemViews[theme.id] = itemBinding

            val root = itemBinding.root
            val targetFlexbox = when {
                theme.colors.size == 3 || theme.id.startsWith(
                    "G3_"
                ) -> dialogBinding.gradient3Flexbox

                theme.id.contains("GRADUAL") || theme.id.startsWith("G2_") || (theme.colors.size == 2 && theme.colors[0] != theme.colors[1]) -> dialogBinding.gradient2Flexbox
                else -> dialogBinding.solidFlexbox
            }
            targetFlexbox.addView(root)

            itemBinding.themeClickArea.setOnClickListener {
                managerScope.launch {
                    activity.multiColorDataStore.edit { p ->
                        val currentSet =
                            p[mainThemesKey]?.toMutableSet() ?: defaultIds.toMutableSet()
                        if (currentSet.contains(theme.id)) {
                            currentSet.remove(theme.id)
                        } else {
                            currentSet.add(theme.id)
                        }
                        p[mainThemesKey] = currentSet
                    }
                }
            }
        }

        activity.lifecycleScope.launch {
            activity.multiColorDataStore.data.collectLatest { prefs ->
                val savedThemeIds = prefs[mainThemesKey]
                val effectiveIds = savedThemeIds ?: defaultIds

                allThemes.forEach { theme ->
                    val itemBinding = itemViews[theme.id] ?: return@forEach
                    val isMain = effectiveIds.contains(theme.id)

                    // Update ONLY the state, extremely fast!
                    itemBinding.themeItemCard.strokeColor = if (isMain) Color.GREEN else Color.RED
                    itemBinding.statusIcon.setImageResource(
                        if (isMain) android.R.drawable.ic_delete
                        else android.R.drawable.ic_input_add
                    )
                    itemBinding.statusIcon.setColorFilter(if (isMain) Color.RED else Color.GREEN)
                }

                // Update headers visibility once
                dialogBinding.tvSolid.visibility =
                    if (dialogBinding.solidFlexbox.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                dialogBinding.tvGradient2.visibility =
                    if (dialogBinding.gradient2Flexbox.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                dialogBinding.tvGradient3.visibility =
                    if (dialogBinding.gradient3Flexbox.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
        }
    }

    fun getThemeBackground(context: Context, theme: MultiColorTheme): Drawable {
        val themeId = theme.id
        val attrId = R.attr.mc_bg

        MultiColorCache.getDrawable(themeId, attrId)?.let { return it }

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

        MultiColorCache.putDrawable(themeId, attrId, drawable)
        return drawable
    }

    private fun resolveThemeDrawable(
        context: Context, theme: android.content.res.Resources.Theme, attrToResolve: Int
    ): Drawable {
        val typedValue = TypedValue()
        val attrIds = intArrayOf(
            attrToResolve, android.R.attr.background, android.R.attr.colorBackground
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
}