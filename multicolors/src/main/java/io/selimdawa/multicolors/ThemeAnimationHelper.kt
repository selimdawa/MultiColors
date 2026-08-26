package io.selimdawa.multicolors

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.HandlerCompat
import kotlin.math.hypot

object ThemeAnimationHelper {
    private var lastScreenshot: Bitmap? = null
    private var capturingActivityClassName: String? = null
    private var currentCaptureId: Long = 0
    var shouldAnimateThemeIcon: Boolean = false
    var animationStartX: Int = 0
    var animationStartY: Int = 0
    var isTransitioning: Boolean = false

    enum class AnimationType {
        INWARD,  // From edges to center (reveals new theme from corners)
        OUTWARD  // From center to edges (reveals new theme from center)
    }

    var animationType: AnimationType = AnimationType.INWARD

    private var lastActionTime: Long = 0
    private const val ACTION_INTERVAL: Long = 1000 // Reduce duration to 1 second

    /**
     * Checks if enough time has passed since the last animated action.
     */
    fun canPerformAction(): Boolean {
        if (isTransitioning) return false
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastActionTime > ACTION_INTERVAL) {
            lastActionTime = currentTime
            return true
        }
        return false
    }

    /**
     * Set the center point for the reveal animation based on a view's location.
     */
    fun setAnimationSource(view: View) {
        val location = IntArray(2)
        view.getLocationInWindow(location)
        animationStartX = location[0] + view.width / 2
        animationStartY = location[1] + view.height / 2
    }

    /**
     * Executes an action (like changing theme or night mode) with a circular reveal animation
     * starting from the provided view.
     */
    fun performAnimatedAction(
        activity: Activity,
        triggerView: View,
        type: AnimationType = AnimationType.INWARD,
        action: () -> Unit
    ) {
        if (!canPerformAction()) return

        this.animationType = type
        setAnimationSource(triggerView)
        captureScreenshot(activity) {
            action()
        }
    }

    fun captureScreenshot(activity: Activity, onComplete: () -> Unit = {}) {
        val view = activity.window.decorView
        if (view.width <= 0 || view.height <= 0) {
            onComplete()
            return
        }

        val captureId = ++currentCaptureId
        // Clear previous screenshot and recycle to free memory
        lastScreenshot?.recycle()
        lastScreenshot = null
        capturingActivityClassName = activity::class.qualifiedName
        val bitmap = createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val location = IntArray(2)
            view.getLocationInWindow(location)
            try {
                PixelCopy.request(
                    activity.window, Rect(
                        location[0],
                        location[1],
                        location[0] + view.width,
                        location[1] + view.height
                    ), bitmap, { copyResult ->
                        if (captureId == currentCaptureId) {
                            if (copyResult == PixelCopy.SUCCESS) {
                                lastScreenshot = bitmap
                            }
                        }
                        onComplete()
                    }, HandlerCompat.createAsync(Looper.getMainLooper())
                )
            } catch (_: IllegalArgumentException) {
                // Fallback to old method
                val canvas = Canvas(bitmap)
                view.draw(canvas)
                lastScreenshot = bitmap
                onComplete()
            }
        } else {
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            lastScreenshot = bitmap
            onComplete()
        }
    }

    fun startThemeChangeAnimation(activity: Activity) {
        isTransitioning = true
        activity.recreate()
        if (activity is AppCompatActivity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                activity.overrideActivityTransition(
                    Activity.OVERRIDE_TRANSITION_OPEN, 0, 0
                )
                activity.overrideActivityTransition(
                    Activity.OVERRIDE_TRANSITION_CLOSE, 0, 0
                )
            } else {
                @Suppress("DEPRECATION") activity.overridePendingTransition(0, 0)
            }
        }
    }

    /**
     * Prepares the activity window to hide the new theme flash by setting the old screenshot
     * as the temporary window background.
     */
    fun prepareTransition(activity: Activity) {
        val screenshot = lastScreenshot
        if (screenshot != null && capturingActivityClassName == activity::class.qualifiedName) {
            activity.window.setBackgroundDrawable(screenshot.toDrawable(activity.resources))
        }
    }

    fun checkAndPerformRevealAnimation(activity: Activity) {
        val screenshot = lastScreenshot
        val activityClass = capturingActivityClassName

        if (screenshot == null || activityClass != activity::class.qualifiedName) {
            cleanupResources()
            return
        }

        val decorView = activity.window.decorView as ViewGroup

        if (animationType == AnimationType.OUTWARD) {
            // OUTWARD: New theme expands from center
            val contentView = if (decorView.childCount > 0) decorView.getChildAt(0) else decorView

            decorView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    decorView.viewTreeObserver.removeOnPreDrawListener(this)
                    if (!activity.isFinishing) {
                        startReveal(contentView, decorView, AnimationType.OUTWARD)
                    }
                    return true
                }
            })
        } else {
            // INWARD: Old theme shrinks to center
            val overlay = ImageView(activity).apply {
                setImageBitmap(screenshot)
                scaleType = ImageView.ScaleType.FIT_XY
                elevation = 9999f
                translationZ = 9999f
            }

            decorView.addView(
                overlay, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )

            decorView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    decorView.viewTreeObserver.removeOnPreDrawListener(this)

                    // Clear the temporary window background
                    activity.window.setBackgroundDrawable(null)

                    if (!activity.isFinishing && overlay.parent != null) {
                        startReveal(overlay, decorView, AnimationType.INWARD)
                    }
                    return true
                }
            })
        }
    }

    private fun startReveal(targetView: View, decorView: ViewGroup, type: AnimationType = animationType) {
        val width = decorView.width
        val height = decorView.height

        if (width <= 0 || height <= 0) {
            decorView.post { startReveal(targetView, decorView, type) }
            return
        }

        val finalRadius = hypot(width.toFloat(), height.toFloat())
        val startRadius = if (type == AnimationType.INWARD) finalRadius else 0f
        val endRadius = if (type == AnimationType.INWARD) 0f else finalRadius

        val anim = ViewAnimationUtils.createCircularReveal(
            targetView, animationStartX, animationStartY, startRadius, endRadius
        )

        anim.duration = 400
        anim.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                if (type == AnimationType.INWARD) {
                    cleanupOverlay(targetView as ImageView)
                } else {
                    val activity = targetView.context as? Activity
                    activity?.window?.setBackgroundDrawable(null)
                    cleanupResources()
                }
            }
            override fun onAnimationCancel(animation: android.animation.Animator) {
                if (type == AnimationType.INWARD) {
                    cleanupOverlay(targetView as ImageView)
                } else {
                    val activity = targetView.context as? Activity
                    activity?.window?.setBackgroundDrawable(null)
                    cleanupResources()
                }
            }
        })
        anim.start()
    }

    private fun cleanupResources() {
        lastScreenshot?.recycle()
        lastScreenshot = null
        capturingActivityClassName = null
        isTransitioning = false
        animationType = AnimationType.INWARD
    }

    private fun cleanupOverlay(overlay: ImageView) {
        if (overlay.parent != null) {
            (overlay.parent as ViewGroup).removeView(overlay)
        }
        shouldAnimateThemeIcon = false
        overlay.setImageDrawable(null)
        isTransitioning = false
    }
}
