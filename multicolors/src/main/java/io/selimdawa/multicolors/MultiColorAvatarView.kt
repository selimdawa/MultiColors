@file:Suppress("unused")

package io.selimdawa.multicolors

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.ShapeAppearanceModel

class MultiColorAvatarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val borderView = MultiColorCircleBorderView(context, attrs).apply {
        id = NO_ID
    }
    val imageView = ShapeableImageView(context).apply {
        id = NO_ID
        strokeWidth = 0f // Fix black edges
        scaleType = ImageView.ScaleType.CENTER_CROP
    }

    var isAnimatingBorder = false
        private set
    var isAnimatingImage = false
        private set

    val isUsingCustomColors: Boolean get() = borderView.isUsingCustomColors

    private var borderDuration = 3000L
    private var imageDuration = 5000L
    private var borderDirection = 1
    private var imageDirection = 1
    private var glowRadius = 0f
    private var imageCornerRadius = -1f

    private var borderAnimator: ObjectAnimator? = null
    private var imageAnimator: ObjectAnimator? = null

    init {
        clipChildren = false
        clipToPadding = false
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.MultiColorAvatarView, 0, 0
        ).apply {
            try {
                isAnimatingBorder =
                    getBoolean(R.styleable.MultiColorAvatarView_mc_animate_border, false)
                isAnimatingImage =
                    getBoolean(R.styleable.MultiColorAvatarView_mc_animate_image, false)
                borderDuration = getInteger(
                    R.styleable.MultiColorAvatarView_mc_border_rotation_duration, 3000
                ).toLong()
                imageDuration = getInteger(
                    R.styleable.MultiColorAvatarView_mc_image_rotation_duration, 5000
                ).toLong()
                borderDirection =
                    getInt(R.styleable.MultiColorAvatarView_mc_border_rotation_direction, 1)
                imageDirection =
                    getInt(R.styleable.MultiColorAvatarView_mc_image_rotation_direction, 1)
                glowRadius = getDimension(R.styleable.MultiColorAvatarView_mc_glow_radius, 0f)
                imageCornerRadius =
                    getDimension(R.styleable.MultiColorAvatarView_mc_image_corner_radius, -1f)

                val scaleTypeIndex = getInt(R.styleable.MultiColorAvatarView_mc_image_scale_type, 6)
                imageView.scaleType = getScaleTypeFromIndex(scaleTypeIndex)

                val imageRes = getResourceId(R.styleable.MultiColorAvatarView_mc_image_src, 0)
                if (imageRes != 0) {
                    imageView.setImageResource(imageRes)
                }

                val imageBg = getDrawable(R.styleable.MultiColorAvatarView_mc_image_background)
                if (imageBg != null) {
                    imageView.background = imageBg
                }

                updateImageShape()

                val thickness =
                    getDimension(R.styleable.MultiColorAvatarView_mc_border_thickness, dpToPx(4f))
                borderView.setBorderThickness(thickness)
                borderView.setGlowRadius(glowRadius)

                val useRainbow = getBoolean(R.styleable.MultiColorAvatarView_mc_use_rainbow, false)
                borderView.setUseRainbow(useRainbow)

                val alwaysWhite =
                    getBoolean(R.styleable.MultiColorAvatarView_mc_always_white, false)
                borderView.setAlwaysWhite(alwaysWhite)

                val contrastSize = getFloat(R.styleable.MultiColorAvatarView_mc_contrast_size, 0.3f)
                borderView.setContrastSize(contrastSize)

                val showContrast =
                    getBoolean(R.styleable.MultiColorAvatarView_mc_show_contrast, false)
                borderView.setShowContrast(showContrast)

                val margin = (thickness + glowRadius).toInt()
                val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                lp.setMargins(margin, margin, margin, margin)
                imageView.layoutParams = lp
                imageView.setPadding(
                    dpToPx(1f).toInt(), dpToPx(1f).toInt(), dpToPx(1f).toInt(), dpToPx(1f).toInt()
                )

            } finally {
                recycle()
            }
        }

        addView(borderView)
        addView(imageView)

        updateAnimations()
    }

    private fun updateImageShape() {
        imageView.shapeAppearanceModel = if (imageCornerRadius >= 0) {
            ShapeAppearanceModel.builder().setAllCornerSizes(imageCornerRadius).build()
        } else {
            ShapeAppearanceModel.builder().setAllCornerSizes(ShapeAppearanceModel.PILL).build()
        }
    }

    private fun getScaleTypeFromIndex(index: Int): ImageView.ScaleType {
        return when (index) {
            0 -> ImageView.ScaleType.MATRIX
            1 -> ImageView.ScaleType.FIT_XY
            2 -> ImageView.ScaleType.FIT_START
            3 -> ImageView.ScaleType.FIT_CENTER
            4 -> ImageView.ScaleType.FIT_END
            5 -> ImageView.ScaleType.CENTER
            6 -> ImageView.ScaleType.CENTER_CROP
            7 -> ImageView.ScaleType.CENTER_INSIDE
            else -> ImageView.ScaleType.CENTER_CROP
        }
    }

    private fun updateAnimations() {
        // Border Animation (Rotating the colors inside the border)
        borderAnimator?.cancel()
        if (isAnimatingBorder) {
            val start = 0f
            val end = 360f * borderDirection
            borderAnimator = ObjectAnimator.ofFloat(borderView, "shaderRotation", start, end).apply {
                duration = borderDuration
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                start()
            }
        }

        // Image Animation (Rotating the actual image view)
        imageAnimator?.cancel()
        if (isAnimatingImage) {
            val start = 0f
            val end = 360f * imageDirection
            imageAnimator = ObjectAnimator.ofFloat(imageView, "rotation", start, end).apply {
                duration = imageDuration
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                start()
            }
        }
    }

    /**
     * Resets the border to use theme or rainbow colors.
     */
    fun resetToThemeColors() {
        borderView.resetToThemeColors()
    }

    fun setAnimateBorder(animate: Boolean) {
        isAnimatingBorder = animate
        updateAnimations()
    }

    fun setAnimateImage(animate: Boolean) {
        isAnimatingImage = animate
        updateAnimations()
    }

    /**
     * Sets a custom list of colors (from 2 to 10) for the border.
     */
    fun setColors(colors: IntArray) {
        borderView.setColors(colors)
    }

    /**
     * Sets a custom list of colors (from 2 to 10) for the border.
     */
    fun setColors(colors: List<Int>) {
        borderView.setColors(colors)
    }

    private fun dpToPx(dp: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics
    )
}