package io.selimdawa.multicolors

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import io.selimdawa.multicolors.databinding.DialogThemeSelectorBinding
import io.selimdawa.multicolors.databinding.ItemThemeBinding
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
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _currentThemeId = MutableStateFlow("")
    val currentThemeId: StateFlow<String> = _currentThemeId.asStateFlow()

    fun init(application: Application) {
        // Read initial value synchronously to avoid race condition on first activity startup
        val savedThemeId = runBlocking {
            application.multiColorDataStore.data.map {
                it[themeKey] ?: ThemeRegistry.getAllThemes().first().id
            }.first()
        }
        _currentThemeId.value = savedThemeId

        managerScope.launch {
            application.multiColorDataStore.data.map {
                it[themeKey] ?: ThemeRegistry.getAllThemes().first().id
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

    fun applyTheme(context: Context) {
        val themeId = _currentThemeId.value.ifEmpty {
            runBlocking {
                context.multiColorDataStore.data.map {
                    it[themeKey] ?: ThemeRegistry.getAllThemes().first().id
                }.first()
            }
        }
        val theme = ThemeRegistry.getTheme(themeId)
        if (theme is MultiColorTheme.Xml) {
            context.setTheme(theme.styleRes)
        } else {
            context.setTheme(R.style.MC_Base_Theme)
        }
    }

    fun showThemeDialog(activity: AppCompatActivity) {
        val themes = ThemeRegistry.getAllThemes()
        val dialogBinding = DialogThemeSelectorBinding.inflate(activity.layoutInflater)
        val dialog = AlertDialog.Builder(activity).setView(dialogBinding.root).create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val width = (activity.resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        themes.forEach { theme ->
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

            if (theme.id.contains("GRADUAL", ignoreCase = true)) {
                dialogBinding.gradientFlexbox.addView(itemBinding.root)
            } else {
                dialogBinding.solidFlexbox.addView(itemBinding.root)
            }
        }
    }

    fun getThemeBackground(context: Context, theme: MultiColorTheme): Drawable {
        val themeId = theme.id
        val attrId = R.attr.mc_bg
        
        MultiColorCache.getDrawable(themeId, attrId)?.let { return it }

        val drawable = when (theme) {
            is MultiColorTheme.Xml -> {
                val customTheme = context.resources.newTheme()
                customTheme.applyStyle(theme.styleRes, true)
                resolveThemeDrawable(context, customTheme, attrId)
            }
        }

        MultiColorCache.putDrawable(themeId, attrId, drawable)
        return drawable
    }

    /**
     * Resolves a color attribute for the current theme with caching.
     */
    fun getColor(context: Context, attrId: Int): Int {
        val currentTheme = getCurrentTheme(context)
        
        MultiColorCache.getColor(currentTheme.id, attrId)?.let { return it }

        val typedValue = TypedValue()
        val theme = context.theme
        val color = if (theme.resolveAttribute(attrId, typedValue, true)) {
            if (typedValue.resourceId != 0) {
                ResourcesCompat.getColor(context.resources, typedValue.resourceId, theme)
            } else {
                typedValue.data
            }
        } else {
            ResourcesCompat.getColor(context.resources, R.color.mc_fallback_color, theme)
        }

        MultiColorCache.putColor(currentTheme.id, attrId, color)
        return color
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
                    it[themeKey] ?: ThemeRegistry.getAllThemes().first().id
                }.first()
            }
        }
        return ThemeRegistry.getTheme(id)
    }
}