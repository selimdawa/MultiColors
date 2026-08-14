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
 * Allows controlling the thickness of the border and adds a neon glow effect.
 */
class MultiColorCircleBorderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var borderThickness = dpToPx(4f)
    private var useRainbow = false
    private var glowRadius = 0f
    private var glowAlpha = 0.5f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null) // Required for BlurMaskFilter
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.MultiColorAvatarView,
            0, 0
        ).apply {
            try {
                borderThickness = getDimension(
                    R.styleable.MultiColorAvatarView_mc_border_thickness,
                    dpToPx(4f)
                )
                useRainbow = getBoolean(
                    R.styleable.MultiColorAvatarView_mc_use_rainbow,
                    false
                )
                glowRadius = getDimension(
                    R.styleable.MultiColorAvatarView_mc_glow_radius,
                    0f
                )
                glowAlpha = getFloat(
                    R.styleable.MultiColorAvatarView_mc_glow_alpha,
                    0.5f
                )
            } finally {
                recycle()
            }
        }
        paint.strokeWidth = borderThickness
        updateGlowSettings()
    }

    private fun updateGlowSettings() {
        if (glowRadius > 0) {
            glowPaint.strokeWidth = borderThickness + (glowRadius * 0.5f)
            glowPaint.maskFilter = BlurMaskFilter(glowRadius, BlurMaskFilter.Blur.NORMAL)
            glowPaint.alpha = (glowAlpha * 255).toInt()
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

    /**
     * Sets the thickness of the colorful border.
     */
    fun setBorderThickness(thicknessInPx: Float) {
        borderThickness = thicknessInPx
        paint.strokeWidth = borderThickness
        updateGlowSettings()
        updateAppearance()
    }

    /**
     * Sets the glow radius for the neon effect.
     */
    fun setGlowRadius(radiusInPx: Float) {
        glowRadius = radiusInPx
        updateGlowSettings()
        invalidate()
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
                glowPaint.shader = gradient
            } else {
                paint.shader = null
                glowPaint.shader = null
                val color = if (colors.isNotEmpty()) colors[0] else Color.GRAY
                paint.color = color
                glowPaint.color = color
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
        val radius = (Math.min(width, height) - borderThickness - (glowRadius * 2)) / 2f
        
        if (glowRadius > 0) {
            canvas.drawCircle(width / 2f, height / 2f, radius, glowPaint)
        }
        canvas.drawCircle(width / 2f, height / 2f, radius, paint)
    }

    private fun dpToPx(dp: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
    )
}
