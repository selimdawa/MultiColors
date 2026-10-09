@file:Suppress("unused")

package io.selimdawa.multicolors

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.annotation.AttrRes
import androidx.appcompat.R as AppCompatR
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes

class MultiColorButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val cardView: MultiColorCardView

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.mc_button_layout, this, true)
        cardView = view.findViewById(R.id.cardView)

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.MultiColorButton, defStyleAttr, 0) {
                if (hasValue(R.styleable.MultiColorButton_mc_card_stroke_color)) {
                    val value = peekValue(R.styleable.MultiColorButton_mc_card_stroke_color)
                    val color = if (value != null && value.type == TypedValue.TYPE_INT_DEC) {
                        when (value.data) {
                            0 -> resolveThemeColor(context, AppCompatR.attr.colorError)
                            1 -> Color.WHITE
                            2 -> resolveThemeColor(context, R.attr.mc_basic)
                            else -> getColor(R.styleable.MultiColorButton_mc_card_stroke_color, Color.WHITE)
                        }
                    } else {
                        getColor(R.styleable.MultiColorButton_mc_card_stroke_color, Color.WHITE)
                    }

                    color?.let { resolvedColor ->
                        cardView.strokeColor = resolvedColor
                    }
                }
            }
        }

        setupListeners()
    }

    fun setStrokeColor(color: Int) {
        cardView.strokeColor = color
    }

    private fun resolveThemeColor(context: Context, @AttrRes attrRes: Int): Int? {
        val typedValue = TypedValue()
        var currentAttr = attrRes
        for (i in 0..5) {
            if (context.theme.resolveAttribute(currentAttr, typedValue, true)) {
                if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                    return typedValue.data
                }
                if (typedValue.resourceId != 0) {
                    val resTypeName = try {
                        context.resources.getResourceTypeName(typedValue.resourceId)
                    } catch (e: Exception) {
                        ""
                    }
                    if (resTypeName == "attr") {
                        currentAttr = typedValue.resourceId
                        continue
                    }
                    return ContextCompat.getColor(context, typedValue.resourceId)
                }
                if (typedValue.data != 0) {
                    return typedValue.data
                }
            }
            break
        }
        return null
    }

    private fun setupListeners() {
        setOnClickListener {
            findActivity(context)?.let { MultiColorManager.showThemeDialog(it) }
        }
    }

    private fun findActivity(context: Context): ComponentActivity? {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is ComponentActivity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }
}