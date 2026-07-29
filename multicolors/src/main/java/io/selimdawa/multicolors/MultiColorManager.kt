package io.selimdawa.multicolors

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.LruCache
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
    private val drawableCache = LruCache<Int, Drawable>(16)
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val defaultThemeMap = mapOf(
        "ONE" to R.style.Theme_MC_One,
        "TWO" to R.style.Theme_MC_Two,
        "THREE" to R.style.Theme_MC_Three,
        "FOUR" to R.style.Theme_MC_Four,
        "FIVE" to R.style.Theme_MC_Five,
        "SIX" to R.style.Theme_MC_Six,
        "SEVEN" to R.style.Theme_MC_Seven,
        "EIGHT" to R.style.Theme_MC_Eight,
        "NINE" to R.style.Theme_MC_Nine,
        "GRADUAL_ONE" to R.style.Theme_MC_Gradient_One,
        "GRADUAL_TWO" to R.style.Theme_MC_Gradient_Two,
        "GRADUAL_THREE" to R.style.Theme_MC_Gradient_Three,
        "GRADUAL_FOUR" to R.style.Theme_MC_Gradient_Four,
        "GRADUAL_FIVE" to R.style.Theme_MC_Gradient_Five,
        "GRADUAL_SIX" to R.style.Theme_MC_Gradient_Six,
        "GRADUAL_SEVEN" to R.style.Theme_MC_Gradient_Seven
    )

    private val _currentTheme = MutableStateFlow("")
    val currentTheme: StateFlow<String> = _currentTheme.asStateFlow()

    fun init(application: Application, themeMap: Map<String, Int> = defaultThemeMap) {
        managerScope.launch {
            application.multiColorDataStore.data.map { it[themeKey] ?: themeMap.keys.first() }
                .collectLatest { _currentTheme.value = it }
        }

        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            private var lastTheme = ""

            override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
                applyTheme(activity, themeMap)
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                (activity as? AppCompatActivity)?.let { appCompatActivity ->
                    appCompatActivity.lifecycleScope.launch {
                        currentTheme.collectLatest { theme ->
                            if (lastTheme.isNotEmpty() && lastTheme != theme) {
                                lastTheme = theme
                                activity.recreate()
                            } else if (lastTheme.isEmpty()) {
                                lastTheme = theme
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

    fun applyTheme(context: Context, themeMap: Map<String, Int> = defaultThemeMap) {
        val option = runBlocking {
            context.multiColorDataStore.data.map { it[themeKey] ?: themeMap.keys.first() }.first()
        }
        context.setTheme(themeMap[option] ?: themeMap.values.first())
    }

    fun showThemeDialog(
        activity: AppCompatActivity,
        themeMap: Map<String, Int> = defaultThemeMap,
        themeNames: Array<String>? = null
    ) {
        val names = themeNames ?: activity.resources.getStringArray(R.array.mc_theme_entries)
        val values = themeMap.keys.toTypedArray()
        val themeResIds = themeMap.values.toIntArray()

        val dialogBinding = DialogThemeSelectorBinding.inflate(activity.layoutInflater)
        val dialog = AlertDialog.Builder(activity).setView(dialogBinding.root).create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val width = (activity.resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        values.forEachIndexed { index, key ->
            val resId = themeResIds[index]
            val name = names[index]

            val itemBinding = ItemThemeBinding.inflate(activity.layoutInflater)
            itemBinding.themeNameText.text = name

            val customTheme = activity.resources.newTheme()
            customTheme.applyStyle(resId, true)
            itemBinding.themeColorView.background = getThemeBackground(activity, customTheme, resId)

            itemBinding.root.setOnClickListener {
                activity.lifecycleScope.launch {
                    activity.multiColorDataStore.edit { prefs -> prefs[themeKey] = key }
                }
                dialog.dismiss()
            }

            if (key.contains("GRADUAL", ignoreCase = true)) {
                dialogBinding.gradientFlexbox.addView(itemBinding.root)
            } else {
                dialogBinding.solidFlexbox.addView(itemBinding.root)
            }
        }
    }

    fun getThemeBackground(context: Context, theme: Resources.Theme, cacheKey: Int = -1): Drawable {
        if (cacheKey != -1) {
            drawableCache.get(cacheKey)?.let { return it }
        }

        val typedValue = TypedValue()
        val attrIds = intArrayOf(
            R.attr.mc_bg, android.R.attr.background, android.R.attr.colorBackground
        )

        var resolved = false
        for (attrId in attrIds) {
            if (attrId != 0 && theme.resolveAttribute(attrId, typedValue, true)) {
                resolved = true
                break
            }
        }

        val drawable = if (resolved && typedValue.resourceId != 0) {
            ResourcesCompat.getDrawable(context.resources, typedValue.resourceId, theme)!!
        } else if (resolved) {
            typedValue.data.toDrawable()
        } else {
            ResourcesCompat.getColor(context.resources, R.color.mc_fallback_color, theme)
                .toDrawable()
        }

        if (cacheKey != -1) {
            drawableCache.put(cacheKey, drawable)
        }
        return drawable
    }
}