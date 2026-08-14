package io.selimdawa.multicolors

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A circular view that draws a gradient border based on the current theme.
 * Allows controlling the thickness of the border.
 */
class MultiColorCircleBorderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var borderThickness = dpToPx(4f)
    private var useRainbow = false
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.MultiColorCircleBorderView,
            0, 0
        ).apply {
            try {
                borderThickness = getDimension(
                    R.styleable.MultiColorCircleBorderView_mc_border_thickness,
                    borderThickness
                )
                useRainbow = getBoolean(
                    R.styleable.MultiColorCircleBorderView_mc_use_rainbow,
                    false
                )
            } finally {
                recycle()
            }
        }
        paint.strokeWidth = borderThickness
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            MultiColorManager.currentThemeId.collectLatest {
                updateAppearance()
            }
        }
    }

    /**
     * Sets the thickness of the colorful border.
     */
    fun setBorderThickness(thicknessInPx: Float) {
        borderThickness = thicknessInPx
        paint.strokeWidth = borderThickness
        updateAppearance()
    }

    /**
     * If true, forces the border to use rainbow colors instead of the theme colors.
     */
    fun setUseRainbow(rainbow: Boolean) {
        useRainbow = rainbow
        updateAppearance()
    }

    private fun updateAppearance() {
        val colors = if (useRainbow) {
            getRainbowColors()
        } else {
            val theme = MultiColorManager.getCurrentTheme(context)
            getThemeColors(theme)
        }
        
        if (width > 0 && height > 0) {
            if (colors.size >= 2) {
                // Ensure the gradient connects smoothly at the start/end
                val sweepColors = if (colors.first() != colors.last()) {
                    val result = IntArray(colors.size + 1)
                    System.arraycopy(colors, 0, result, 0, colors.size)
                    result[colors.size] = colors[0]
                    result
                } else {
                    colors
                }

                val gradient = SweepGradient(width / 2f, height / 2f, sweepColors, null)
                val matrix = Matrix()
                matrix.postRotate(-90f, width / 2f, height / 2f)
                gradient.setLocalMatrix(matrix)
                paint.shader = gradient
            } else {
                paint.shader = null
                paint.color = if (colors.isNotEmpty()) colors[0] else Color.GRAY
            }
        }
        invalidate()
    }

    private fun getThemeColors(theme: MultiColorTheme): IntArray {
        if (theme.colors.isNotEmpty()) return theme.colors.toIntArray()
        
        val styleRes = theme.styleRes ?: return getRainbowColors()
        
        val typedValue = TypedValue()
        val c = context.resources.newTheme().apply { applyStyle(styleRes, true) }
        
        val colorList = mutableListOf<Int>()
        val attrsToResolve = intArrayOf(R.attr.mc_track, R.attr.mc_center, R.attr.mc_tick)
        
        attrsToResolve.forEach { attr ->
            if (c.resolveAttribute(attr, typedValue, true)) {
                val color = if (typedValue.resourceId != 0) {
                    ResourcesCompat.getColor(context.resources, typedValue.resourceId, c)
                } else {
                    typedValue.data
                }
                colorList.add(color)
            }
        }
        
        if (colorList.isEmpty()) {
            val standardAttrs = intArrayOf(androidx.appcompat.R.attr.colorPrimary, androidx.appcompat.R.attr.colorAccent)
            standardAttrs.forEach { attr ->
                if (c.resolveAttribute(attr, typedValue, true)) {
                    val color = if (typedValue.resourceId != 0) {
                        ResourcesCompat.getColor(context.resources, typedValue.resourceId, c)
                    } else {
                        typedValue.data
                    }
                    colorList.add(color)
                }
            }
        }

        return if (colorList.isEmpty()) getRainbowColors() else colorList.toIntArray()
    }

    private fun getRainbowColors() = intArrayOf(
        Color.parseColor("#FF0000"), // Red
        Color.parseColor("#FF7F00"), // Orange
        Color.parseColor("#FFFF00"), // Yellow
        Color.parseColor("#00FF00"), // Green
        Color.parseColor("#0000FF"), // Blue
        Color.parseColor("#4B0082"), // Indigo
        Color.parseColor("#8B00FF")  // Violet
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateAppearance()
    }

    override fun onDraw(canvas: Canvas) {
        val radius = (Math.min(width, height) - borderThickness) / 2f
        canvas.drawCircle(width / 2f, height / 2f, radius, paint)
    }

    private fun dpToPx(dp: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
    )
}
