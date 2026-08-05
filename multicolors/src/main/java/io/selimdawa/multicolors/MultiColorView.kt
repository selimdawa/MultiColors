package io.selimdawa.multicolors

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

open class MultiColorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    protected val mcInnerView: View = View(context).apply {
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
        )
    }

    init {
        setCardBackgroundColor(Color.TRANSPARENT)
        if (childCount == 0) {
            addView(mcInnerView)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            MultiColorManager.currentThemeId.collectLatest {
                updateAppearance()
            }
        }
    }

    protected open fun updateAppearance() {
        val theme = MultiColorManager.getCurrentTheme(context)
        mcInnerView.background = MultiColorManager.getThemeBackground(context, theme)
    }
}