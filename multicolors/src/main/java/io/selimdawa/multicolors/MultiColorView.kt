package io.selimdawa.multicolors

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.view.isEmpty
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

open class MultiColorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    protected val mcInnerView: ShapeableImageView = ShapeableImageView(context).apply {
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
        )
        scaleType = ImageView.ScaleType.CENTER_CROP
    }

    init {
        setCardBackgroundColor(Color.TRANSPARENT)
        clipToOutline = true
        preventCornerOverlap = false
        if (isEmpty()) {
            addView(mcInnerView)
        }
    }

    override fun setShapeAppearanceModel(shapeAppearanceModel: ShapeAppearanceModel) {
        super.setShapeAppearanceModel(shapeAppearanceModel)
        mcInnerView.shapeAppearanceModel = shapeAppearanceModel
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mcInnerView.shapeAppearanceModel = shapeAppearanceModel
        findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            MultiColorManager.currentThemeId.collectLatest {
                updateAppearance()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        updateAppearance()
    }

    protected open fun updateAppearance() {
        val theme = MultiColorManager.getCurrentTheme(context)
        mcInnerView.setImageDrawable(MultiColorManager.getThemeBackground(context, theme))
    }
}