package io.selimdawa.multicolors

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

/**
 * A specialized view that draws an animated border with two colors: Red and Blue.
 * Now supports glow effect and corner radius.
 */
class RedBlueAnimatedBorderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var borderThickness = context.dpToPx(4f)
    private var cornerRadius = context.dpToPx(12f)
    private var glowRadius = 0f
    private var glowAlpha = 0.5f
    private var rotationAngle = 0f
    
    private val redColor = Color.RED
    private val blueColor = Color.BLUE

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
    private var animator: ValueAnimator? = null

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.MultiColorBorderLayout, 0, 0
        ).apply {
            try {
                borderThickness = getDimension(R.styleable.MultiColorBorderLayout_mc_border_thickness, context.dpToPx(4f))
                cornerRadius = getDimension(R.styleable.MultiColorBorderLayout_mc_corner_radius, context.dpToPx(12f))
                glowRadius = getDimension(R.styleable.MultiColorBorderLayout_mc_glow_radius, 0f)
                glowAlpha = getFloat(R.styleable.MultiColorBorderLayout_mc_glow_alpha, 0.5f)
                val duration = getInteger(R.styleable.MultiColorBorderLayout_mc_border_rotation_duration, 2000).toLong()
                startAnimation(duration)
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

    private fun startAnimation(durationMs: Long) {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = durationMs
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                rotationAngle = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateShader()
    }

    private fun updateShader() {
        if (width > 0 && height > 0) {
            val colors = intArrayOf(redColor, blueColor, redColor)
            val positions = floatArrayOf(0f, 0.5f, 1f)
            val shader = SweepGradient(width / 2f, height / 2f, colors, positions)
            paint.shader = shader
            glowPaint.shader = shader
        }
    }

    override fun onDraw(canvas: Canvas) {
        val inset = borderThickness / 2f + glowRadius
        rect.set(inset, inset, width - inset, height - inset)

        paint.shader?.let { shader ->
            matrix.setRotate(rotationAngle - 90f, width / 2f, height / 2f)
            shader.setLocalMatrix(matrix)
            glowPaint.shader?.setLocalMatrix(matrix)
        }

        if (glowRadius > 0) {
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glowPaint)
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
    }

    fun setBorderThickness(thicknessDp: Float) {
        borderThickness = context.dpToPx(thicknessDp)
        paint.strokeWidth = borderThickness
        updateGlowSettings()
        updateShader()
        invalidate()
    }

    fun setCornerRadius(radiusDp: Float) {
        cornerRadius = context.dpToPx(radiusDp)
        invalidate()
    }

    fun setAnimationDuration(durationMs: Long) {
        startAnimation(durationMs)
    }

    fun setGlowRadius(radiusDp: Float) {
        glowRadius = context.dpToPx(radiusDp)
        updateGlowSettings()
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}
