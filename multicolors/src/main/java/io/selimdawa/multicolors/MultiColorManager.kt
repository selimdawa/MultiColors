package io.selimdawa.multicolors

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.util.AttributeSet
import android.util.LruCache
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.imageview.ShapeableImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.LayoutInflaterCompat
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
import java.lang.reflect.Method

val Context.multiColorDataStore by preferencesDataStore(name = "multicolor_prefs")

object MultiColorManager {
    private val themeKey = stringPreferencesKey("color_option")
    private val drawableCache = LruCache<String, Drawable>(16)
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _currentThemeId = MutableStateFlow("")
    val currentThemeId: StateFlow<String> = _currentThemeId.asStateFlow()

    fun init(application: Application) {
        managerScope.launch {
            application.multiColorDataStore.data.map {
                it[themeKey] ?: ThemeRegistry.getAllThemes().first().id
            }.collectLatest { _currentThemeId.value = it }
        }

        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {

            override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
                applyTheme(activity)
                installFactory(activity)
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                (activity as? AppCompatActivity)?.let { appCompatActivity ->
                    var lastThemeId = ""
                    appCompatActivity.lifecycleScope.launch {
                        currentThemeId.collectLatest { themeId ->
                            if (lastThemeId.isNotEmpty() && lastThemeId != themeId) {
                                lastThemeId = themeId
                                activity.recreate()
                            } else {
                                lastThemeId = themeId
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

    private fun installFactory(activity: Activity) {
        if (activity !is AppCompatActivity) return
        val inflater = activity.layoutInflater
        if (inflater.factory2 == null) {
            LayoutInflaterCompat.setFactory2(inflater, object : LayoutInflater.Factory2 {
                override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? {
                    return createViewAndApply(activity, parent, name, context, attrs)
                }

                override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
                    return createViewAndApply(activity, null, name, context, attrs)
                }
            })
        }
    }

    private fun createViewAndApply(activity: AppCompatActivity, parent: View?, name: String, context: Context, attrs: AttributeSet): View? {
        var view = activity.delegate.createView(parent, name, context, attrs)
        if (view == null) {
            try {
                if (name.contains('.')) {
                    view = activity.layoutInflater.createView(name, null, attrs)
                } else {
                    for (prefix in arrayOf("android.widget.", "android.view.", "android.webkit.")) {
                        view = activity.layoutInflater.createView(name, prefix, attrs)
                        if (view != null) break
                    }
                }
            } catch (e: Exception) { }
        }
        return view?.also { applyDynamicColors(it, context, attrs) }
    }

    private fun applyDynamicColors(view: View, context: Context, attrs: AttributeSet) {
        val theme = getCurrentTheme(context)
        if (theme is MultiColorTheme.Dynamic) {
            val color = theme.solidColor
            val colorList = ColorStateList.valueOf(color)

            for (i in 0 until attrs.attributeCount) {
                val attrName = attrs.getAttributeName(i)
                val attrValue = attrs.getAttributeValue(i)

                if (attrValue != null && attrValue.startsWith("?")) {
                    val resId = try {
                        if (attrValue.startsWith("?attr/")) {
                            context.resources.getIdentifier(attrValue.substring(6), "attr", context.packageName)
                        } else {
                            attrValue.substring(1).toInt()
                        }
                    } catch (e: Exception) { 0 }

                    if (resId != 0) {
                        val entryName = try { context.resources.getResourceEntryName(resId) } catch (e: Exception) { "" }
                        if (entryName == "mc_bg" || entryName == "mc_track" || entryName == "mc_tick") {
                            applyColorToViewAttr(view, attrName, color, colorList)
                        }
                    }
                } else if (attrValue != null && attrValue.startsWith("@")) {
                    // Check if it's a drawable that might need theming
                    if (attrName.contains("background", ignoreCase = true) || 
                        attrName.contains("src", ignoreCase = true) ||
                        attrName.contains("thumb", ignoreCase = true) ||
                        attrName.contains("track", ignoreCase = true) ||
                        attrName.contains("progress", ignoreCase = true)) {
                        
                        val resId = attrs.getAttributeResourceValue(i, 0)
                        if (resId != 0) {
                            val typeName = try { context.resources.getResourceTypeName(resId) } catch (e: Exception) { "" }
                            if (typeName == "drawable") {
                                // For Dynamic theme, we assume any custom drawable on these attributes should be themed
                                view.post {
                                    when (attrName) {
                                        "background" -> view.background?.let { themeCustomDrawable(it, color) }
                                        "src" -> if (view is ImageView) view.drawable?.let { themeCustomDrawable(it, color) }
                                        "scrollbarThumbVertical" -> try {
                                            val method = View::class.java.getDeclaredMethod("getVerticalScrollbarThumbDrawable")
                                            method.isAccessible = true
                                            (method.invoke(view) as? Drawable)?.let { themeCustomDrawable(it, color) }
                                        } catch (e: Exception) {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun applyColorToViewAttr(view: View, attrName: String, color: Int, colorList: ColorStateList) {
        when {
            attrName == "background" || attrName.contains("backgroundTint") -> {
                if (attrName == "background") view.setBackgroundColor(color)
                else view.backgroundTintList = colorList
            }
            attrName == "textColor" -> if (view is TextView) view.setTextColor(color)
            attrName == "tint" || attrName == "src" -> {
                if (view is ImageView) {
                    if (attrName == "src") view.setImageDrawable(color.toDrawable())
                    else view.imageTintList = colorList
                }
            }
            attrName.contains("progressTint") -> if (view is android.widget.ProgressBar) view.progressTintList = colorList
            attrName.contains("thumbTint") -> if (view is android.widget.AbsSeekBar) view.thumbTintList = colorList
            attrName.contains("strokeColor") -> {
                if (view is ShapeableImageView) view.strokeColor = colorList
            }
        }
    }

    private fun themeCustomDrawable(drawable: Drawable, color: Int) {
        val d = drawable.mutate()
        if (d is LayerDrawable) {
            // For our specific border drawables, the first layer is the gradient/solid that needs theming
            if (d.numberOfLayers > 0) {
                d.getDrawable(0).setTint(color)
            }
        } else {
            d.setTint(color)
        }
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

            itemBinding.root.setOnClickListener {
                val newThemeId = theme.id
                _currentThemeId.value = newThemeId
                activity.lifecycleScope.launch {
                    activity.multiColorDataStore.edit { prefs -> prefs[themeKey] = newThemeId }
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
        drawableCache.get(theme.id)?.let { return it }

        val drawable = when (theme) {
            is MultiColorTheme.Dynamic -> theme.solidColor.toDrawable()
            is MultiColorTheme.Xml -> {
                val customTheme = context.resources.newTheme()
                customTheme.applyStyle(theme.styleRes, true)
                resolveThemeDrawable(context, customTheme, R.attr.mc_bg)
            }
        }

        drawableCache.put(theme.id, drawable)
        return drawable
    }

    fun getColor(context: Context, attrId: Int): Int {
        val theme = getCurrentTheme(context)
        return when (theme) {
            is MultiColorTheme.Dynamic -> {
                when (attrId) {
                    R.attr.mc_bg, R.attr.mc_track, R.attr.mc_tick -> theme.solidColor
                    else -> resolveFromSystem(context, attrId)
                }
            }
            is MultiColorTheme.Xml -> resolveThemeColor(context, theme.styleRes, attrId)
        }
    }

    private fun resolveFromSystem(context: Context, attrId: Int): Int {
        val typedValue = TypedValue()
        return if (context.theme.resolveAttribute(attrId, typedValue, true)) {
            if (typedValue.resourceId != 0) {
                ResourcesCompat.getColor(context.resources, typedValue.resourceId, context.theme)
            } else {
                typedValue.data
            }
        } else {
            ResourcesCompat.getColor(context.resources, R.color.mc_fallback_color, context.theme)
        }
    }

    fun getColorBg(context: Context) = getColor(context, R.attr.mc_bg)
    fun getColorTrack(context: Context) = getColor(context, R.attr.mc_track)
    fun getColorTick(context: Context) = getColor(context, R.attr.mc_tick)

    private fun resolveThemeColor(context: Context, styleRes: Int, attrId: Int): Int {
        val customTheme = context.resources.newTheme()
        customTheme.applyStyle(styleRes, true)
        val typedValue = TypedValue()
        return if (customTheme.resolveAttribute(attrId, typedValue, true)) {
            if (typedValue.resourceId != 0) {
                try {
                    ResourcesCompat.getColor(context.resources, typedValue.resourceId, customTheme)
                } catch (e: Exception) {
                    ResourcesCompat.getColor(
                        context.resources, R.color.mc_fallback_color, customTheme
                    )
                }
            } else {
                typedValue.data
            }
        } else {
            ResourcesCompat.getColor(context.resources, R.color.mc_fallback_color, customTheme)
        }
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
