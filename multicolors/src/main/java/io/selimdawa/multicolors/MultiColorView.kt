package io.selimdawa.multicolors

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.util.AttributeSet
import android.widget.ImageView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A base card-based view that automatically reacts to theme changes.
 */
open class MultiColorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private var themeJob: Job? = null

    protected val mcInnerView: ShapeableImageView = ShapeableImageView(context).apply {
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
        )
        scaleType = ImageView.ScaleType.CENTER_CROP
    }

    init {
        clipToOutline = true
        clipChildren = false
        clipToPadding = false
        preventCornerOverlap = false
        // Add mcInnerView as the first child to serve as the background
        if (mcInnerView.parent == null) {
            addView(mcInnerView, 0)
        }
    }

    override fun setShapeAppearanceModel(shapeAppearanceModel: ShapeAppearanceModel) {
        super.setShapeAppearanceModel(shapeAppearanceModel)
        mcInnerView.shapeAppearanceModel = shapeAppearanceModel
    }

    private fun setupThemeCollector() {
        val owner = findViewTreeLifecycleOwner()
        if (owner == null) {
            // Wait for lifecycle owner to be available
            post { setupThemeCollector() }
            return
        }

        themeJob?.cancel()
        themeJob = owner.lifecycleScope.launch {
            MultiColorManager.currentThemeId.collectLatest {
                val activity = findActivity(context)
                // PREVENT FLASH: Do not update views in the old activity that is being destroyed
                if (activity != null && activity.isFinishing) return@collectLatest
                
                updateAppearance()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mcInnerView.shapeAppearanceModel = shapeAppearanceModel
        setupThemeCollector()
    }

    override fun onDetachedFromWindow() {
        themeJob?.cancel()
        themeJob = null
        super.onDetachedFromWindow()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        updateAppearance()
    }

    protected open fun updateAppearance() {
        val theme = MultiColorManager.getCurrentTheme(context)
        val background = MultiColorManager.getThemeBackground(context, theme)
        
        mcInnerView.translationY = 0f
        mcInnerView.alpha = 1f
        mcInnerView.setImageDrawable(background)
    }

    private fun findActivity(context: Context): Activity? {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) return currentContext
            currentContext = currentContext.baseContext
        }
        return null
    }
}
