@file:Suppress("unused")

package io.selimdawa.multicolors

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * A rectangular view that draws a gradient border with rounded corners.
 * Supports theme colors, rainbow mode, and neon glow.
 */
class MultiColorRectBorderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var themeJob: Job? = null
    private var borderThickness = context.dpToPx(2f)
    private var cornerRadius = context.dpToPx(8f)
    private var useRainbow = false
    private var alwaysWhite = false
    private var showContrast = false
    private var contrastSize = 0.3f
    private var customColors: IntArray? = null
    private var glowRadius = 0f
    private var glowAlpha = 0.5f
    
    /**
     * The rotation of the gradient colors in degrees.
     * Animated by MultiColorBorderLayout.
     */
    var shaderRotation: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    val isUsingCustomColors: Boolean get() = customColors != null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val rect = RectF()
    private val matrix = Matrix()

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.MultiColorBorderLayout, 0, 0
        ).apply {
            try {
                borderThickness = getDimension(R.styleable.MultiColorBorderLayout_mc_border_thickness, context.dpToPx(2f))
                cornerRadius = getDimension(R.styleable.MultiColorBorderLayout_mc_corner_radius, context.dpToPx(8f))
                useRainbow = getBoolean(R.styleable.MultiColorBorderLayout_mc_use_rainbow, false)
                alwaysWhite = getBoolean(R.styleable.MultiColorBorderLayout_mc_always_white, false)
                showContrast = getBoolean(R.styleable.MultiColorBorderLayout_mc_show_contrast, false)
                contrastSize = getFloat(R.styleable.MultiColorBorderLayout_mc_contrast_size, 0.3f)
                glowRadius = getDimension(R.styleable.MultiColorBorderLayout_mc_glow_radius, 0f)
                glowAlpha = getFloat(R.styleable.MultiColorBorderLayout_mc_glow_alpha, 0.5f)
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
        themeJob?.cancel()
        themeJob = findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            MultiColorManager.currentThemeId.collectLatest {
                updateAppearance()
            }
        }
    }

    override fun onDetachedFromWindow() {
        themeJob?.cancel()
        themeJob = null
        super.onDetachedFromWindow()
    }

    fun setBorderThickness(thicknessInPx: Float) {
        borderThickness = thicknessInPx
        paint.strokeWidth = borderThickness
        updateGlowSettings()
        updateAppearance()
    }

    fun setCornerRadius(radiusInPx: Float) {
        cornerRadius = radiusInPx
        invalidate()
    }

    fun setColors(colors: IntArray) {
        if (colors.size in 2..10) {
            this.customColors = colors
            this.useRainbow = false
            updateAppearance()
        }
    }

    fun setColors(colors: List<Int>) = setColors(colors.toIntArray())

    fun resetToThemeColors() {
        this.customColors = null
        updateAppearance()
    }

    fun setShowContrast(show: Boolean) {
        showContrast = show
        updateAppearance()
    }

    fun setGlowRadius(radiusInPx: Float) {
        glowRadius = radiusInPx
        updateGlowSettings()
        invalidate()
    }

    private fun updateAppearance() {
        val colors = customColors ?: if (useRainbow) getRainbowColors() else {
            val theme = MultiColorManager.getCurrentTheme(context)
            getThemeColors(theme)
        }

        if (width > 0 && height > 0) {
            if (colors.size >= 2) {
                var sweepColors = colors
                var positions: FloatArray? = null

                if (colors.size == 3 && colors[0] == colors[2]) {
                    val halfSize = contrastSize / 2f
                    sweepColors = intArrayOf(colors[0], colors[0], colors[1], colors[2], colors[2])
                    positions = floatArrayOf(0f, 0.5f - halfSize, 0.5f, 0.5f + halfSize, 1f)
                } else if (colors.first() != colors.last()) {
                    val result = IntArray(colors.size + 1)
                    System.arraycopy(colors, 0, result, 0, colors.size)
                    result[colors.size] = colors[0]
                    sweepColors = result
                }

                val gradient = SweepGradient(width / 2f, height / 2f, sweepColors, positions)
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
        val colors = MultiColorManager.getThemeColors(context, theme)

        val isNightMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val contrastColor = if (alwaysWhite) Color.WHITE else (if (isNightMode) Color.WHITE else Color.BLACK)

        return if (colors.size == 1 || (colors.size == 2 && colors[0] == colors[1])) {
            if (showContrast) intArrayOf(colors[0], contrastColor, colors[0])
            else intArrayOf(colors[0], colors[0])
        } else {
            colors
        }
    }

    private fun getRainbowColors() = intArrayOf(
        "#FF0000".toColorInt(), "#FF7F00".toColorInt(), "#FFFF00".toColorInt(),
        "#00FF00".toColorInt(), "#0000FF".toColorInt(), "#4B0082".toColorInt(), "#8B00FF".toColorInt()
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateAppearance()
    }

    override fun onDraw(canvas: Canvas) {
        val inset = borderThickness / 2f + glowRadius
        rect.set(inset, inset, width - inset, height - inset)

        // Apply rotation only to the colors (shader), not the canvas
        val shader = paint.shader
        if (shader != null) {
            matrix.setRotate(shaderRotation - 90f, width / 2f, height / 2f)
            shader.setLocalMatrix(matrix)
            glowPaint.shader?.setLocalMatrix(matrix)
        }

        if (glowRadius > 0) {
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glowPaint)
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
    }
}
