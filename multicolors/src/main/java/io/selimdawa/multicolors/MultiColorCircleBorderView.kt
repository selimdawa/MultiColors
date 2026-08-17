@file:Suppress("unused")

package io.selimdawa.multicolors

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * A circular view that draws a gradient border based on the current theme.
 * Allows controlling the thickness of the border and adds a neon glow effect.
 */
class MultiColorCircleBorderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var borderThickness = dpToPx(4f)
    private var useRainbow = false
    private var alwaysWhite = false
    private var showContrast = false
    private var contrastSize = 0.3f
    private var customColors: IntArray? = null
    private var glowRadius = 0f
    private var glowAlpha = 0.5f
    
    /**
     * The rotation of the gradient colors in degrees.
     * Animated by MultiColorAvatarView.
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

    private val matrix = Matrix()

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null) // Required for BlurMaskFilter
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.MultiColorAvatarView, 0, 0
        ).apply {
            try {
                borderThickness = getDimension(
                    R.styleable.MultiColorAvatarView_mc_border_thickness, dpToPx(4f)
                )
                useRainbow = getBoolean(
                    R.styleable.MultiColorAvatarView_mc_use_rainbow, false
                )
                alwaysWhite = getBoolean(
                    R.styleable.MultiColorAvatarView_mc_always_white, false
                )
                showContrast = getBoolean(
                    R.styleable.MultiColorAvatarView_mc_show_contrast, false
                )
                contrastSize = getFloat(
                    R.styleable.MultiColorAvatarView_mc_contrast_size, 0.3f
                )
                glowRadius = getDimension(
                    R.styleable.MultiColorAvatarView_mc_glow_radius, 0f
                )
                glowAlpha = getFloat(
                    R.styleable.MultiColorAvatarView_mc_glow_alpha, 0.5f
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
     * Sets the size of the contrast color (0.0 to 1.0).
     */
    fun setContrastSize(size: Float) {
        contrastSize = size.coerceIn(0f, 1f)
        updateAppearance()
    }

    /**
     * Sets whether to show the contrast color in solid themes.
     */
    fun setShowContrast(show: Boolean) {
        showContrast = show
        updateAppearance()
    }

    /**
     * Sets whether to always use white as the contrast color for solid themes.
     */
    fun setAlwaysWhite(always: Boolean) {
        alwaysWhite = always
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
     * Sets a custom list of colors (strictly from 2 to 10) for the border.
     * If the list size is outside this range, the call is ignored.
     */
    fun setColors(colors: IntArray) {
        if (colors.size in 2..10) {
            this.customColors = colors
            this.useRainbow = false
            updateAppearance()
        }
    }

    /**
     * Sets a custom list of colors (from 2 to 10) for the border.
     * This will override the current theme colors.
     */
    fun setColors(colors: List<Int>) {
        setColors(colors.toIntArray())
    }

    /**
     * Resets the border to use theme or rainbow colors.
     */
    fun resetToThemeColors() {
        this.customColors = null
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
        val colors = customColors ?: if (useRainbow) {
            getRainbowColors()
        } else {
            val theme = MultiColorManager.getCurrentTheme(context)
            getThemeColors(theme)
        }

        if (width > 0 && height > 0) {
            if (colors.size >= 2) {
                var sweepColors = colors
                var positions: FloatArray? = null

                // Handle contrast size for [Solid, Contrast, Solid] cases
                if (colors.size == 3 && colors[0] == colors[2]) {
                    val halfSize = contrastSize / 2f
                    sweepColors = intArrayOf(colors[0], colors[0], colors[1], colors[2], colors[2])
                    positions = floatArrayOf(0f, 0.5f - halfSize, 0.5f, 0.5f + halfSize, 1f)
                } else if (colors.first() != colors.last()) {
                    // Ensure the gradient connects smoothly at the start/end
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
        
        val isNightMode = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val contrastColor = if (alwaysWhite) Color.WHITE else (if (isNightMode) Color.WHITE else Color.BLACK)

        return if (colors.size == 1 || (colors.size == 2 && colors[0] == colors[1])) {
            if (showContrast) intArrayOf(colors[0], contrastColor, colors[0])
            else intArrayOf(colors[0], colors[0])
        } else {
            colors
        }
    }

    private fun getRainbowColors() = intArrayOf(
        "#FF0000".toColorInt(), // Red
        "#FF7F00".toColorInt(), // Orange
        "#FFFF00".toColorInt(), // Yellow
        "#00FF00".toColorInt(), // Green
        "#0000FF".toColorInt(), // Blue
        "#4B0082".toColorInt(), // Indigo
        "#8B00FF".toColorInt()  // Violet
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateAppearance()
    }

    override fun onDraw(canvas: Canvas) {
        val radius = (min(width, height) - borderThickness - (glowRadius * 2)) / 2f

        // Apply rotation only to the colors (shader), not the canvas
        val shader = paint.shader
        if (shader != null) {
            matrix.setRotate(shaderRotation - 90f, width / 2f, height / 2f)
            shader.setLocalMatrix(matrix)
            glowPaint.shader?.setLocalMatrix(matrix)
        }

        if (glowRadius > 0) {
            canvas.drawCircle(width / 2f, height / 2f, radius, glowPaint)
        }
        canvas.drawCircle(width / 2f, height / 2f, radius, paint)
    }

    private fun dpToPx(dp: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
    )
}