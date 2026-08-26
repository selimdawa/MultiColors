package io.selimdawa.multicolors

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Looper
import android.view.PixelCopy
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.os.HandlerCompat
import kotlin.math.hypot

object ThemeAnimationHelper {
    private var lastScreenshot: Bitmap? = null
    private var capturingActivityClassName: String? = null
    private var currentCaptureId: Long = 0
    var animationStartX: Int = 0
    var animationStartY: Int = 0

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

    fun checkAndPerformRevealAnimation(activity: Activity) {
        val screenshot = lastScreenshot

        // If the activity doesn't match, or there's no screenshot, cleanup and return
        if (screenshot == null || capturingActivityClassName != activity::class.qualifiedName) {
            lastScreenshot?.recycle()
            lastScreenshot = null
            capturingActivityClassName = null
            return
        }

        lastScreenshot = null
        capturingActivityClassName = null

        val decorView = activity.window.decorView as ViewGroup
        val overlay = ImageView(activity).apply {
            setImageBitmap(screenshot)
            scaleType = ImageView.ScaleType.FIT_XY
        }

        // Add overlay to the absolute top of the window
        decorView.addView(
            overlay, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Wait for the new layout to be ready
        overlay.postDelayed({
            if (!activity.isFinishing && overlay.parent != null) {
                val width = decorView.width
                val height = decorView.height
                val finalRadius = hypot(width.toFloat(), height.toFloat())

                val anim = ViewAnimationUtils.createCircularReveal(
                    overlay, animationStartX, animationStartY, finalRadius, 0f
                )

                anim.duration = 800
                anim.addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        cleanupOverlay(overlay)
                    }

                    override fun onAnimationCancel(animation: android.animation.Animator) {
                        cleanupOverlay(overlay)
                    }
                })
                anim.start()
            }
        }, 50)
    }

    private fun cleanupOverlay(overlay: ImageView) {
        if (overlay.parent != null) {
            (overlay.parent as ViewGroup).removeView(overlay)
        }
        // Free the bitmap memory
        overlay.setImageDrawable(null)
    }
}