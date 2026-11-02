package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisCyanPrimary
import com.example.ui.theme.JarvisGoldAccent
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ArcReactorVisualizer(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    isActive: Boolean = true,
    isSpeaking: Boolean = false,
    audioLevel: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reactor_anim")

    // Animations without causing parent recomposition
    val outerRotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rot"
    )

    val innerRotation = infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rot"
    )

    val breathingPulse = infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val speakingGlow = infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speaking_glow"
    )

    val precomputedTicks = remember {
        val numTicks = 24
        List(numTicks) { i ->
            val angle = (i * 360f / numTicks) * (PI.toFloat() / 180f)
            val cosA = cos(angle)
            val sinA = sin(angle)
            val isGold = i % 6 == 0
            Triple(cosA, sinA, isGold)
        }
    }

    val precomputedCoils = remember {
        val numCoils = 10
        List(numCoils) { i ->
            val angle = (i * 360f / numCoils) * (PI.toFloat() / 180f)
            cos(angle) to sin(angle)
        }
    }

    val reusableTrianglePath = remember { Path() }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val rotOuter = outerRotation.value
            val rotInner = innerRotation.value
            val pulse = if (isSpeaking) {
                1.05f + (speakingGlow.value * 0.08f)
            } else if (audioLevel > 0.05f) {
                1f + (audioLevel * 0.2f)
            } else if (isActive) {
                breathingPulse.value
            } else {
                0.92f
            }

            val canvasCenter = Offset(this.size.width / 2f, this.size.height / 2f)
            val maxRadius = min(this.size.width, this.size.height) / 2f * 0.88f * pulse

            val primaryCyan = if (isSpeaking) JarvisCyanLight else JarvisCyanPrimary
            val glowColor = if (isSpeaking) JarvisCyanPrimary.copy(alpha = 0.5f) else JarvisCyanDark.copy(alpha = 0.3f)

            // 1. Ambient outer glow circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = canvasCenter,
                    radius = maxRadius * 1.25f
                ),
                radius = maxRadius * 1.25f,
                center = canvasCenter
            )

            // 2. Outermost boundary ring
            drawCircle(
                color = primaryCyan.copy(alpha = 0.4f),
                radius = maxRadius,
                center = canvasCenter,
                style = Stroke(width = 2.dp.toPx())
            )

            // 3. Segmented HUD tick marks on outer ring (Rotated)
            rotate(rotOuter, pivot = canvasCenter) {
                val r1 = maxRadius - 10.dp.toPx()
                val r2 = maxRadius - 2.dp.toPx()
                for (tick in precomputedTicks) {
                    val cosA = tick.first
                    val sinA = tick.second
                    val isGold = tick.third

                    val start = Offset(canvasCenter.x + r1 * cosA, canvasCenter.y + r1 * sinA)
                    val end = Offset(canvasCenter.x + r2 * cosA, canvasCenter.y + r2 * sinA)

                    val tickColor = if (isGold) JarvisGoldAccent else primaryCyan.copy(alpha = 0.7f)
                    val strokeW = if (isGold) 3.5.dp.toPx() else 1.8.dp.toPx()
                    drawLine(color = tickColor, start = start, end = end, strokeWidth = strokeW, cap = StrokeCap.Round)
                }

                // Arc segments
                drawArc(
                    color = primaryCyan,
                    startAngle = 15f,
                    sweepAngle = 60f,
                    useCenter = false,
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = primaryCyan,
                    startAngle = 105f,
                    sweepAngle = 60f,
                    useCenter = false,
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = primaryCyan,
                    startAngle = 195f,
                    sweepAngle = 60f,
                    useCenter = false,
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = primaryCyan,
                    startAngle = 285f,
                    sweepAngle = 60f,
                    useCenter = false,
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 4. Middle Ring with counter-rotation
            val midRadius = maxRadius * 0.68f
            drawCircle(
                color = JarvisCyanLight.copy(alpha = 0.5f),
                radius = midRadius,
                center = canvasCenter,
                style = Stroke(width = 2.5.dp.toPx())
            )

            rotate(rotInner, pivot = canvasCenter) {
                val r1 = midRadius - 7.dp.toPx()
                val r2 = midRadius + 7.dp.toPx()
                for (coil in precomputedCoils) {
                    val p1 = Offset(canvasCenter.x + r1 * coil.first, canvasCenter.y + r1 * coil.second)
                    val p2 = Offset(canvasCenter.x + r2 * coil.first, canvasCenter.y + r2 * coil.second)
                    drawLine(
                        color = if (isSpeaking) JarvisCyanLight else JarvisCyanPrimary,
                        start = p1,
                        end = p2,
                        strokeWidth = 3.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 5. Central Arc Reactor Core
            val coreRadius = maxRadius * 0.38f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        JarvisCyanLight,
                        JarvisCyanPrimary.copy(alpha = 0.8f),
                        Color.Transparent
                    ),
                    center = canvasCenter,
                    radius = coreRadius * 1.1f
                ),
                radius = coreRadius,
                center = canvasCenter
            )

            // 6. Core geometric inverted triangle
            rotate(rotOuter * 0.5f, pivot = canvasCenter) {
                val triRadius = coreRadius * 0.65f
                reusableTrianglePath.reset()
                for (i in 0..2) {
                    val angle = (i * 120f - 90f) * (PI.toFloat() / 180f)
                    val pt = Offset(canvasCenter.x + triRadius * cos(angle), canvasCenter.y + triRadius * sin(angle))
                    if (i == 0) reusableTrianglePath.moveTo(pt.x, pt.y) else reusableTrianglePath.lineTo(pt.x, pt.y)
                }
                reusableTrianglePath.close()

                drawPath(
                    path = reusableTrianglePath,
                    color = Color.White.copy(alpha = 0.95f),
                    style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                )

                // Inner core dot
                drawCircle(
                    color = Color.White,
                    radius = 3.5.dp.toPx(),
                    center = canvasCenter
                )
            }
        }
    }
}
