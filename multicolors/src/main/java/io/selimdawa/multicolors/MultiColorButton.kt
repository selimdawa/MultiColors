package io.selimdawa.multicolors

import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.activity.ComponentActivity

class MultiColorButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.mc_button_layout, this, true)
        setupListeners()
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