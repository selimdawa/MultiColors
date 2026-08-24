package io.selimdawa.multicolors

import android.content.Context
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.shape.RelativeCornerSize
import com.google.android.material.shape.ShapeAppearanceModel

class MultiColorButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MultiColorView(context, attrs, defStyleAttr) {

    init {
        setupDefaultStyle()
        setupListeners()
    }

    private fun setupDefaultStyle() {
        shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCornerSizes(RelativeCornerSize(0.5f))
            .build()
        setCardBackgroundColor(ColorStateList.valueOf(Color.TRANSPARENT))
        strokeColor = ContextCompat.getColor(context, R.color.mc_border_color)
        strokeWidth = dpToPx(1f).toInt()
        elevation = 0f
        preventCornerOverlap = false
    }

    private var lastClickTime: Long = 0
    private val clickInterval: Long = 1000 // 1 second

    private fun setupListeners() {
        setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > clickInterval) {
                lastClickTime = currentTime
                findAppCompatActivity(context)?.let { MultiColorManager.showThemeDialog(it) }
            }
        }
    }

    private fun findAppCompatActivity(context: Context): AppCompatActivity? {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is AppCompatActivity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }

    private fun dpToPx(dp: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
    )
}