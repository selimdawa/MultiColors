package io.selimdawa.multicolors

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.graphics.BlurMaskFilter
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * CompositionLocal to provide the current MultiColorTheme.
 */
val LocalMultiColorTheme = compositionLocalOf<MultiColorTheme> {
    error("No MultiColorTheme provided")
}

/**
 * A Composable wrapper that provides the current MultiColor theme to its content.
 */
@Composable
fun MultiColorTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val currentThemeId by MultiColorManager.currentThemeId.collectAsState()

    val theme = remember(currentThemeId) {
        MultiColorManager.getCurrentTheme(context)
    }

    CompositionLocalProvider(LocalMultiColorTheme provides theme) {
        content()
    }
}

/**
 * A Composable that displays a circular border with theme colors.
 */
@Composable
fun MultiColorCircleBorder(
    modifier: Modifier = Modifier,
    thickness: Dp = 4.dp,
    glowRadius: Dp = 0.dp,
    glowAlpha: Float = 0.5f,
    animate: Boolean = false,
    animationDuration: Int = 3000,
    useRainbow: Boolean = false,
    customColors: List<Color>? = null
) {
    val context = LocalContext.current
    val theme = MultiColorCompose.theme
    val colors = remember(theme, useRainbow, customColors) {
        customColors
            ?: if (useRainbow) MultiColorCompose.rainbowColors else MultiColorManager.getThemeColors(
                context,
                theme
            ).map { Color(it) }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "MultiColor_Border_Rotation")
    val rotation by if (animate) {
        infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
                animation = tween(animationDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "Rotation"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    Canvas(modifier = modifier) {
        val sweepColors = if (colors.size >= 2 && colors.first() != colors.last()) {
            colors + colors.first()
        } else colors

        val brush = Brush.sweepGradient(sweepColors)
        val strokeWidth = thickness.toPx()
        val glowPx = glowRadius.toPx()

        drawIntoCanvas { canvas ->
            if (glowPx > 0f) {
                val frameworkPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    this.style = android.graphics.Paint.Style.STROKE
                    this.strokeWidth = strokeWidth + (glowPx * 0.5f)
                    this.maskFilter = BlurMaskFilter(glowPx, BlurMaskFilter.Blur.NORMAL)
                    this.alpha = (glowAlpha * 255).toInt()
                }

                canvas.nativeCanvas.save()
                canvas.nativeCanvas.rotate(rotation - 90f, size.width / 2, size.height / 2)

                val shaderColors = sweepColors.map { it.toArgb() }.toIntArray()
                frameworkPaint.shader = android.graphics.SweepGradient(
                    size.width / 2, size.height / 2, shaderColors, null
                )

                canvas.nativeCanvas.drawCircle(
                    size.width / 2,
                    size.height / 2,
                    (size.minDimension - strokeWidth - glowPx * 2) / 2,
                    frameworkPaint
                )
                canvas.nativeCanvas.restore()
            }

            canvas.nativeCanvas.save()
            canvas.nativeCanvas.rotate(rotation - 90f, size.width / 2, size.height / 2)

            drawCircle(
                brush = brush,
                radius = (size.minDimension - strokeWidth - glowPx * 2) / 2,
                style = Stroke(width = strokeWidth)
            )
            canvas.nativeCanvas.restore()
        }
    }
}

/**
 * A Composable that displays a rectangular border with theme colors and rounded corners.
 */
@Composable
fun MultiColorRectBorder(
    modifier: Modifier = Modifier,
    thickness: Dp = 2.dp,
    cornerRadius: Dp = 8.dp,
    glowRadius: Dp = 0.dp,
    glowAlpha: Float = 0.5f,
    animate: Boolean = false,
    animationDuration: Int = 3000,
    useRainbow: Boolean = false,
    customColors: List<Color>? = null
) {
    val context = LocalContext.current
    val theme = MultiColorCompose.theme
    val colors = remember(theme, useRainbow, customColors) {
        customColors
            ?: if (useRainbow) MultiColorCompose.rainbowColors else MultiColorManager.getThemeColors(
                context,
                theme
            ).map { Color(it) }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "MultiColor_Rect_Rotation")
    val rotation by if (animate) {
        infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
                animation = tween(animationDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "Rotation"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    Canvas(modifier = modifier) {
        val sweepColors = if (colors.size >= 2 && colors.first() != colors.last()) {
            colors + colors.first()
        } else colors

        val strokeWidth = thickness.toPx()
        val glowPx = glowRadius.toPx()
        val cornerPx = cornerRadius.toPx()

        drawIntoCanvas { canvas ->
            val inset = strokeWidth / 2f + glowPx + 1f
            val rect = android.graphics.RectF(inset, inset, size.width - inset, size.height - inset)

            val shaderColors = sweepColors.map { it.toArgb() }.toIntArray()
            val shader =
                android.graphics.SweepGradient(size.width / 2, size.height / 2, shaderColors, null)
            val matrix = android.graphics.Matrix()
            matrix.postRotate(rotation - 90f, size.width / 2, size.height / 2)
            shader.setLocalMatrix(matrix)

            if (glowPx > 0f) {
                val glowPaint =
                    android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        this.style = android.graphics.Paint.Style.STROKE
                        this.strokeWidth = strokeWidth + (glowPx * 0.5f)
                        this.maskFilter = BlurMaskFilter(glowPx, BlurMaskFilter.Blur.NORMAL)
                        this.alpha = (glowAlpha * 255).toInt()
                        this.shader = shader
                    }
                canvas.nativeCanvas.drawRoundRect(rect, cornerPx, cornerPx, glowPaint)
            }

            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                this.style = android.graphics.Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                this.shader = shader
            }
            canvas.nativeCanvas.drawRoundRect(rect, cornerPx, cornerPx, paint)
        }
    }
}

/**
 * A professional Composable Avatar with animated MultiColor border.
 */
@Composable
fun MultiColorAvatar(
    modifier: Modifier = Modifier,
    image: @Composable () -> Unit,
    borderThickness: Dp = 4.dp,
    glowRadius: Dp = 4.dp,
    animateBorder: Boolean = true,
    useRainbow: Boolean = false,
    shape: Shape = CircleShape
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        MultiColorCircleBorder(
            modifier = Modifier.matchParentSize(),
            thickness = borderThickness,
            glowRadius = glowRadius,
            animate = animateBorder,
            useRainbow = useRainbow
        )

        val padding = borderThickness + glowRadius + 2.dp
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .clip(shape)
        ) {
            image()
        }
    }
}

/**
 * A specialized Composable for toggling Night/Light mode with built-in 
 * Telegram-style animations and automatic theme handling.
 */
@Composable
fun MultiColorNightModeButton(
    lightIconRes: Int,
    darkIconRes: Int,
    modifier: Modifier = Modifier,
    iconColorMode: Int = 1 // 0: track mode, 1: adaptive mode
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isNightMode = (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
    
    val trackColor = MultiColorCompose.colors.firstOrNull() ?: Color.Gray

    val tint = when (iconColorMode) {
        0 -> trackColor
        else -> if (isNightMode) Color.White else Color.Black
    }

    IconButton(
        onClick = {
            val activity = findActivity(context) ?: return@IconButton
            val animationType = if (isNightMode)
                NightModeAnimationHelper.AnimationType.INWARD else NightModeAnimationHelper.AnimationType.OUTWARD

            NightModeAnimationHelper.performAnimatedAction(
                activity, activity.window.decorView, animationType
            ) {
                val newMode = if (isNightMode) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
                MultiColorManager.setNightMode(context, newMode)
            }
        },
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(if (isNightMode) lightIconRes else darkIconRes),
            contentDescription = "Toggle Night Mode",
            tint = tint
        )
    }
}

/**
 * A container that draws a rotating colorful border around its content.
 */
@Composable
fun MultiColorBorderBox(
    modifier: Modifier = Modifier,
    thickness: Dp = 2.dp,
    cornerRadius: Dp = 8.dp,
    glowRadius: Dp = 0.dp,
    animate: Boolean = true,
    animationDuration: Int = 3000,
    content: @Composable () -> Unit
) {
    val padding = thickness + (glowRadius * 1.5f)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        MultiColorRectBorder(
            modifier = Modifier.matchParentSize(),
            thickness = thickness,
            cornerRadius = cornerRadius,
            glowRadius = glowRadius,
            animate = animate,
            animationDuration = animationDuration
        )
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}

/**
 * A Composable that displays a box with the current MultiColorTheme background.
 */
@Composable
fun MultiColorBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(0.dp),
    content: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier.multiColorBackground(shape), contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * A Composable Button that uses the current MultiColorTheme.
 */
@Composable
fun MultiColorButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .multiColorBackground(shape)
            .clickable(onClick = onClick)
            .padding(contentPadding), contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Modifier that applies the current MultiColorTheme background to the Composable.
 */
@Composable
fun Modifier.multiColorBackground(shape: Shape = RoundedCornerShape(0.dp)): Modifier {
    return this.background(MultiColorCompose.brush, shape)
}

/**
 * Modifier that applies the current MultiColorTheme as a border to the Composable.
 */
@Composable
fun Modifier.multiColorBorder(
    width: Dp = 2.dp, shape: Shape = RoundedCornerShape(0.dp)
): Modifier {
    return this.border(width, MultiColorCompose.brush, shape)
}

/**
 * Access the current MultiColorTheme properties.
 */
object MultiColorCompose {
    val theme: MultiColorTheme
        @Composable @ReadOnlyComposable get() = LocalMultiColorTheme.current

    val colors: List<Color>
        @Composable get() {
            val context = LocalContext.current
            val theme = theme
            return remember(theme) {
                MultiColorManager.getThemeColors(context, theme).map { Color(it) }
            }
        }

    val brush: Brush
        @Composable get() {
            val colors = colors
            val theme = theme
            return remember(colors, theme) {
                createBrush(colors, theme.orientation)
            }
        }

    val rainbowColors = listOf(
        Color(0xFFFF0000.toInt()),
        Color(0xFFFF7F00.toInt()),
        Color(0xFFFFFF00.toInt()),
        Color(0xFF00FF00.toInt()),
        Color(0xFF0000FF.toInt()),
        Color(0xFF4B0082.toInt()),
        Color(0xFF8B00FF.toInt())
    )

    /**
     * Access the current MultiColorTheme colors with animation.
     */
    @Composable
    fun animatedColors(durationMillis: Int = 500): List<Color> {
        val targetColors = colors
        val animatedColors = remember { mutableStateListOf<Color>() }

        LaunchedEffect(targetColors) {
            if (animatedColors.isEmpty()) {
                animatedColors.addAll(targetColors)
            }
        }

        return targetColors.mapIndexed { index, color ->
            animateColorAsState(
                targetValue = color,
                animationSpec = tween(durationMillis),
                label = "MultiColor_Color_$index"
            ).value
        }
    }

    /**
     * Access the current MultiColorTheme brush with animation.
     */
    @Composable
    fun animatedBrush(durationMillis: Int = 500): Brush {
        val colors = animatedColors(durationMillis)
        val theme = theme
        return remember(colors, theme) {
            createBrush(colors, theme.orientation)
        }
    }

    private fun createBrush(colors: List<Color>, orientation: GradientDrawable.Orientation): Brush {
        if (colors.isEmpty()) return Brush.linearGradient(
            listOf(
                Color.Transparent, Color.Transparent
            )
        )
        if (colors.size == 1) return Brush.verticalGradient(listOf(colors[0], colors[0]))

        return when (orientation) {
            GradientDrawable.Orientation.TOP_BOTTOM -> Brush.verticalGradient(colors)
            GradientDrawable.Orientation.TR_BL -> Brush.linearGradient(colors)
            GradientDrawable.Orientation.RIGHT_LEFT -> Brush.horizontalGradient(colors.reversed())
            GradientDrawable.Orientation.BR_TL -> Brush.linearGradient(colors.reversed())
            GradientDrawable.Orientation.BOTTOM_TOP -> Brush.verticalGradient(colors.reversed())
            GradientDrawable.Orientation.BL_TR -> Brush.linearGradient(colors)
            GradientDrawable.Orientation.LEFT_RIGHT -> Brush.horizontalGradient(colors)
            GradientDrawable.Orientation.TL_BR -> Brush.linearGradient(colors)
        }
    }
}

private fun findActivity(context: Context): Activity? {
    var currentContext = context
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}