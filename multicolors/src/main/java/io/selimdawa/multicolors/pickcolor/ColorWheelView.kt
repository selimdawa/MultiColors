package io.selimdawa.multicolors.pickcolor

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class ColorWheelView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var huePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var svPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var selectorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.WHITE
    }

    private var hueWheelRadius = 0f
    private var innerWheelRadius = 0f
    private var centerX = 0f
    private var centerY = 0f

    private var hue = 0f
    private var saturation = 1f
    private var value = 1f

    private var colorChangeListener: ((Int) -> Unit)? = null

    private val svRect = RectF()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        hueWheelRadius = min(w, h) / 2f - 20f
        innerWheelRadius = hueWheelRadius * 0.8f
        
        val svSize = (innerWheelRadius * sqrt(2f)) / 2f
        svRect.set(centerX - svSize, centerY - svSize, centerX + svSize, centerY + svSize)
        
        setupHuePaint()
    }

    private fun setupHuePaint() {
        val colors = IntArray(361) { i ->
            Color.HSVToColor(floatArrayOf(i.toFloat(), 1f, 1f))
        }
        val gradient = SweepGradient(centerX, centerY, colors, null)
        huePaint.shader = gradient
        huePaint.style = Paint.Style.STROKE
        huePaint.strokeWidth = hueWheelRadius - innerWheelRadius
    }

    override fun onDraw(canvas: Canvas) {
        // Draw Hue Wheel
        canvas.drawCircle(centerX, centerY, (hueWheelRadius + innerWheelRadius) / 2f, huePaint)

        // Draw SV Square
        drawSVSquare(canvas)

        // Draw Selectors
        drawSelectors(canvas)
    }

    private fun drawSVSquare(canvas: Canvas) {
        val saturationGradient = LinearGradient(
            svRect.left, svRect.top, svRect.right, svRect.top,
            Color.WHITE, Color.HSVToColor(floatArrayOf(hue, 1f, 1f)),
            Shader.TileMode.CLAMP
        )
        
        val valueGradient = LinearGradient(
            svRect.left, svRect.top, svRect.left, svRect.bottom,
            Color.TRANSPARENT, Color.BLACK,
            Shader.TileMode.CLAMP
        )

        svPaint.shader = saturationGradient
        canvas.drawRect(svRect, svPaint)
        
        svPaint.shader = valueGradient
        canvas.drawRect(svRect, svPaint)
    }

    private fun drawSelectors(canvas: Canvas) {
        // Hue Selector
        val angle = Math.toRadians(hue.toDouble())
        val r = (hueWheelRadius + innerWheelRadius) / 2f
        val hx = centerX + r * cos(angle).toFloat()
        val hy = centerY + r * sin(angle).toFloat()
        
        selectorPaint.color = if (value < 0.5f) Color.WHITE else Color.BLACK
        canvas.drawCircle(hx, hy, 10f, selectorPaint)

        // SV Selector
        val sx = svRect.left + saturation * svRect.width()
        val sy = svRect.top + (1f - value) * svRect.height()
        canvas.drawCircle(sx, sy, 10f, selectorPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val dx = x - centerX
                val dy = y - centerY
                val dist = sqrt(dx * dx + dy * dy)

                if (dist > innerWheelRadius && dist < hueWheelRadius) {
                    // Hue selection
                    hue = (Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 360f) % 360f
                    updateColor()
                } else if (svRect.contains(x, y)) {
                    // SV selection
                    saturation = (x - svRect.left) / svRect.width()
                    value = 1f - (y - svRect.top) / svRect.height()
                    updateColor()
                }
            }
            MotionEvent.ACTION_UP -> {
                performClick()
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun updateColor() {
        invalidate()
        colorChangeListener?.invoke(getColor())
    }

    fun getColor(): Int {
        return Color.HSVToColor(floatArrayOf(hue, saturation, value))
    }

    fun setColor(color: Int) {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
        invalidate()
    }

    fun setOnColorChangeListener(listener: (Int) -> Unit) {
        colorChangeListener = listener
    }
}