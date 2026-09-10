@file:Suppress("unused", "DiscouragedApi")

package io.selimdawa.multicolors

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.graphics.BlurMaskFilter
import android.graphics.Matrix
import android.graphics.SweepGradient
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

/**
 * CompositionLocal to provide the current MultiColorTheme data.
 */
val LocalMultiColorTheme = compositionLocalOf<MultiColorTheme?> { null }

/**
 * Data class to hold the colors of the current theme for Compose.
 */
data class MultiColorColorScheme(
    val background: Color,
    val track: Color,
    val tick: Color,
    val center: Color,
    val primary: Color,
    val onBackground: Color,
    val error: Color,
    val imageBackground: Color
)

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

    CompositionLocalProvider(
        LocalMultiColorTheme provides theme
    ) {
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
    rotationDirection: Int = 1,
    useRainbow: Boolean = false,
    alwaysWhite: Boolean = false,
    showContrast: Boolean = false,
    contrastSize: Float = 0.3f,
    customColors: List<Color>? = null
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val theme = MultiColorCompose.theme
    val colors = remember(
        theme,
        useRainbow,
        customColors,
        alwaysWhite,
        showContrast,
        contrastSize,
        configuration.uiMode
    ) {
        customColors ?: if (useRainbow) {
            MultiColorCompose.rainbowColors
        } else {
            val themeColors = MultiColorManager.getThemeColors(context, theme).map { Color(it) }
            val isNightMode =
                (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val contrastColor =
                if (alwaysWhite) Color.White else if (isNightMode) Color.White else Color.Black

            if (themeColors.size == 1 || (themeColors.size == 2 && themeColors[0] == themeColors[1])) {
                if (showContrast) listOf(themeColors[0], contrastColor, themeColors[0])
                else listOf(themeColors[0], themeColors[0])
            } else {
                themeColors
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "MultiColor_Border_Rotation")
    val rotation by if (animate) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f * rotationDirection,
            animationSpec = infiniteRepeatable(
                animation = tween(animationDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "Rotation"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
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
                val frameworkPaint =
                    android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        this.style = android.graphics.Paint.Style.STROKE
                        this.strokeWidth = strokeWidth + (glowPx * 0.5f)
                        this.maskFilter = BlurMaskFilter(glowPx, BlurMaskFilter.Blur.NORMAL)
                        this.alpha = (glowAlpha * 255).toInt()
                    }

                canvas.nativeCanvas.save()
                canvas.nativeCanvas.rotate(rotation - 90f, size.width / 2, size.height / 2)

                val shaderColors = sweepColors.map { it.toArgb() }.toIntArray()

                // Handle contrast size positions if colors size is 3 (solid + contrast)
                if (colors.size == 3 && colors[0] == colors[2]) {
                    val halfSize = contrastSize / 2f
                    val expandedColors = intArrayOf(
                        shaderColors[0],
                        shaderColors[0],
                        shaderColors[1],
                        shaderColors[2],
                        shaderColors[2]
                    )
                    val positions = floatArrayOf(0f, 0.5f - halfSize, 0.5f, 0.5f + halfSize, 1f)
                    frameworkPaint.shader = SweepGradient(
                        size.width / 2, size.height / 2, expandedColors, positions
                    )
                } else {
                    frameworkPaint.shader = SweepGradient(
                        size.width / 2, size.height / 2, shaderColors, null
                    )
                }

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

            // For the main border, we can use the same logic if it's solid+contrast
            if (colors.size == 3 && colors[0] == colors[2]) {
                val halfSize = contrastSize / 2f
                drawCircle(
                    brush = Brush.sweepGradient(
                        0f to colors[0],
                        (0.5f - halfSize) to colors[0],
                        0.5f to colors[1],
                        (0.5f + halfSize) to colors[2],
                        1f to colors[2]
                    ),
                    radius = (size.minDimension - strokeWidth - glowPx * 2) / 2,
                    style = Stroke(width = strokeWidth)
                )
            } else {
                drawCircle(
                    brush = brush,
                    radius = (size.minDimension - strokeWidth - glowPx * 2) / 2,
                    style = Stroke(width = strokeWidth)
                )
            }
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
    rotationDirection: Int = 1,
    useRainbow: Boolean = false,
    alwaysWhite: Boolean = false,
    showContrast: Boolean = false,
    contrastSize: Float = 0.3f,
    customColors: List<Color>? = null
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val theme = MultiColorCompose.theme
    val colors = remember(
        theme,
        useRainbow,
        customColors,
        alwaysWhite,
        showContrast,
        contrastSize,
        configuration.uiMode
    ) {
        customColors ?: if (useRainbow) {
            MultiColorCompose.rainbowColors
        } else {
            val themeColors = MultiColorManager.getThemeColors(context, theme).map { Color(it) }
            val isNightMode =
                (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val contrastColor =
                if (alwaysWhite) Color.White else if (isNightMode) Color.White else Color.Black

            if (themeColors.size == 1 || (themeColors.size == 2 && themeColors[0] == themeColors[1])) {
                if (showContrast) listOf(themeColors[0], contrastColor, themeColors[0])
                else listOf(themeColors[0], themeColors[0])
            } else {
                themeColors
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "MultiColor_Rect_Rotation")
    val rotation by if (animate) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f * rotationDirection,
            animationSpec = infiniteRepeatable(
                animation = tween(animationDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "Rotation"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
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

            val matrix = Matrix()
            matrix.postRotate(rotation - 90f, size.width / 2, size.height / 2)

            val shader = if (colors.size == 3 && colors[0] == colors[2]) {
                val halfSize = contrastSize / 2f
                val expandedColors = intArrayOf(
                    shaderColors[0],
                    shaderColors[0],
                    shaderColors[1],
                    shaderColors[2],
                    shaderColors[2]
                )
                val positions = floatArrayOf(0f, 0.5f - halfSize, 0.5f, 0.5f + halfSize, 1f)
                SweepGradient(size.width / 2, size.height / 2, expandedColors, positions)
            } else {
                SweepGradient(size.width / 2, size.height / 2, shaderColors, null)
            }
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
    glowRadius: Dp = 0.dp,
    glowAlpha: Float = 0.5f,
    animateBorder: Boolean = false,
    animateImage: Boolean = false,
    borderRotationDuration: Int = 3000,
    imageRotationDuration: Int = 5000,
    borderRotationDirection: Int = 1,
    imageRotationDirection: Int = 1,
    useRainbow: Boolean = false,
    alwaysWhite: Boolean = false,
    showContrast: Boolean = false,
    contrastSize: Float = 0.3f,
    customColors: List<Color>? = null,
    imageBackground: Color = Color.Transparent,
    shape: Shape = CircleShape
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MultiColor_Avatar_Image_Rotation")
    val imageRotation by if (animateImage) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f * imageRotationDirection,
            animationSpec = infiniteRepeatable(
                animation = tween(imageRotationDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ImageRotation"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        MultiColorCircleBorder(
            modifier = Modifier.matchParentSize(),
            thickness = borderThickness,
            glowRadius = glowRadius,
            glowAlpha = glowAlpha,
            animate = animateBorder,
            animationDuration = borderRotationDuration,
            rotationDirection = borderRotationDirection,
            useRainbow = useRainbow,
            alwaysWhite = alwaysWhite,
            showContrast = showContrast,
            contrastSize = contrastSize,
            customColors = customColors
        )

        val padding = borderThickness + glowRadius + 1.dp
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .graphicsLayer(rotationZ = imageRotation)
                .background(imageBackground, shape)
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
    val isNightMode =
        (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    val trackColor = MultiColorCompose.colors.firstOrNull() ?: Color.Gray
    val tint = when (iconColorMode) {
        0 -> trackColor
        else -> if (isNightMode) Color.White else Color.Black
    }

    // Icon rotation animation
    var iconRotation by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = iconRotation, animationSpec = tween(400), label = "NightModeButtonRotation"
    )

    var positionInWindow by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    IconButton(onClick = {
        if (NightModeAnimationHelper.isTransitioning) return@IconButton
        val activity = findActivity(context) ?: return@IconButton

        // Start icon rotation
        if (isNightMode) iconRotation += 180f

        val animationType =
            if (isNightMode) NightModeAnimationHelper.AnimationType.INWARD else NightModeAnimationHelper.AnimationType.OUTWARD

        val startX = (positionInWindow.x + size.width / 2f).toInt()
        val startY = (positionInWindow.y + size.height / 2f).toInt()

        // Small delay for Sun rotation (matching View version's 100ms)
        val delay = if (isNightMode) 100L else 0L

        val performAction = {
            val newMode =
                if (isNightMode) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES

            NightModeAnimationHelper.performAnimatedAction(
                activity, startX, startY, animationType
            ) {
                MultiColorManager.setNightMode(context, newMode)
                // MultiColorManager now handles automatic recreation for all activity types
            }
        }

        if (delay > 0) {
            activity.window.decorView.postDelayed({ performAction() }, delay)
        } else {
            performAction()
        }
    }, modifier = modifier.onGloballyPositioned { coordinates ->
        positionInWindow = coordinates.positionInWindow()
        size = coordinates.size
    }) {
        Icon(
            painter = painterResource(if (isNightMode) lightIconRes else darkIconRes),
            contentDescription = "Toggle Night Mode",
            tint = tint,
            modifier = Modifier.graphicsLayer(rotationZ = animatedRotation)
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
    rotationDirection: Int = 1,
    useRainbow: Boolean = false,
    alwaysWhite: Boolean = false,
    showContrast: Boolean = false,
    contrastSize: Float = 0.3f,
    customColors: List<Color>? = null,
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
            animationDuration = animationDuration,
            rotationDirection = rotationDirection,
            useRainbow = useRainbow,
            alwaysWhite = alwaysWhite,
            showContrast = showContrast,
            contrastSize = contrastSize,
            customColors = customColors
        )
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
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
    /**
     * Internal helper to resolve theme colors efficiently in Compose.
     */
    @Composable
    private fun rememberColor(attrId: Int): Color {
        val context = LocalContext.current
        val themeId by MultiColorManager.currentThemeId.collectAsState()
        return remember(themeId, attrId) {
            resolveColorAttr(context, attrId)
        }
    }

    @Composable
    private fun rememberColor(attrName: String): Color {
        val context = LocalContext.current
        val themeId by MultiColorManager.currentThemeId.collectAsState()
        return remember(themeId, attrName) {
            resolveColorAttr(context, attrName)
        }
    }

    /** Shortcut for mc_bg (Theme Background Gradient) */
    val mc_bg @Composable get() = brush

    /** Shortcut for mc_track */
    val mc_track @Composable get() = rememberColor(R.attr.mc_track)

    /** Shortcut for mc_tick */
    val mc_tick @Composable get() = rememberColor(R.attr.mc_tick)

    /** Shortcut for mc_gradient as a Brush */
    val mc_gradient: Brush @Composable get() {
        val theme = theme
        val colors = colors
        return remember(colors, theme) {
            // Ensure we only use 2 colors for mc_gradient to keep it "straight"
            val gradientColors = if (colors.size >= 2) {
                listOf(colors.first(), colors.last())
            } else if (colors.isNotEmpty()) {
                listOf(colors[0], colors[0])
            } else {
                listOf(Color.Transparent, Color.Transparent)
            }
            Brush.verticalGradient(gradientColors)
        }
    }

    /** Shortcut for mc_gradient_3 as a Brush */
    val mc_gradient_3: Brush @Composable get() {
        val track = mc_track
        val center = mc_center
        val tick = mc_tick
        return remember(track, center, tick) {
            Brush.verticalGradient(listOf(track, center, tick))
        }
    }

    /** Shortcut for mc_center */
    val mc_center @Composable get() = rememberColor(R.attr.mc_center)

    /** Shortcut for colorPrimary */
    val colorPrimary @Composable get() = rememberColor("colorPrimary")

    /** Shortcut for colorOnBackground */
    val colorOnBackground @Composable get() = rememberColor("colorOnBackground")

    /** Shortcut for colorError */
    val colorError @Composable get() = rememberColor("colorError")

    /** Shortcut for mc_image_background */
    val mc_image_background @Composable get() = rememberColor(R.attr.mc_image_background)

    val theme: MultiColorTheme
        @Composable get() {
            val context = LocalContext.current
            val providedTheme = LocalMultiColorTheme.current
            val currentThemeId by MultiColorManager.currentThemeId.collectAsState()

            return remember(providedTheme, currentThemeId) {
                providedTheme ?: MultiColorManager.getCurrentTheme(context)
            }
        }

    val colors: List<Color>
        @Composable get() {
            val context = LocalContext.current
            val theme = theme
            val uiMode = LocalConfiguration.current.uiMode
            return remember(theme, uiMode) {
                MultiColorManager.getThemeColors(context, theme).map { Color(it) }
            }
        }

    val brush: Brush
        @Composable get() {
            val colors = colors
            val theme = theme
            return remember(colors, theme) {
                // Check if this is explicitly defined as a 3-color theme
                val isExplicitlyThreeColors = theme.id.startsWith("G3_") ||
                        theme.colors.size == 3 ||
                        theme.darkColors.size == 3

                // If it has 3 colors but isn't explicitly a 3-color theme, 
                // it might be inheriting a default 'mc_center' color. 
                // We filter it for mc_bg to keep 2-color gradients clean.
                val effectiveColors = if (colors.size == 3 && !isExplicitlyThreeColors) {
                    listOf(colors.first(), colors.last())
                } else {
                    colors
                }
                createBrush(effectiveColors, theme.orientation)
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

private fun resolveColorAttr(context: Context, attrId: Int): Color {
    val typedValue = TypedValue()
    return if (context.theme.resolveAttribute(attrId, typedValue, true)) {
        if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            Color(typedValue.data)
        } else if (typedValue.resourceId != 0) {
            Color(ContextCompat.getColor(context, typedValue.resourceId))
        } else {
            Color.Unspecified
        }
    } else {
        Color.Unspecified
    }
}

private fun resolveColorAttr(context: Context, attrName: String): Color {
    var attrId = context.resources.getIdentifier(attrName, "attr", context.packageName)
    if (attrId == 0) {
        attrId = context.resources.getIdentifier(attrName, "attr", "io.selimdawa.multicolors")
    }
    return if (attrId != 0) resolveColorAttr(context, attrId) else Color.Unspecified
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