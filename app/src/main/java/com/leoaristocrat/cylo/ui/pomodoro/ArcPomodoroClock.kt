package com.leoaristocrat.cylo.ui.pomodoro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Arc Clock Style: Time as measurable progress.
 * A single, strong circular progress arc with a precision leading marker,
 * framing a mathematically centered time display.
 */
@Composable
fun ArcPomodoroClock(
    remainingSeconds: Int,
    totalSeconds: Int = 25 * 60,
    timerStatus: TimerStatus = TimerStatus.IDLE,
    modifier: Modifier = Modifier
) {
    val isRunning = timerStatus == TimerStatus.RUNNING
    val isPaused = timerStatus == TimerStatus.PAUSED
    val isCompleted = remainingSeconds <= 0 && timerStatus != TimerStatus.IDLE
    val seconds = remainingSeconds % 60
    val isFinalMinute = remainingSeconds in 1..59

    // Authoritative progress sweep (0° to 360°)
    val targetSweep = if (totalSeconds > 0) {
        (remainingSeconds.toFloat() / totalSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f) * 360f
    } else 0f

    val sweepAnimatable = remember { Animatable(targetSweep) }

    LaunchedEffect(targetSweep, isRunning) {
        val diff = abs(targetSweep - sweepAnimatable.value)
        if (diff > 20f || !isRunning) {
            // Instant snap for resets, restarts, duration changes, or when paused/idle
            sweepAnimatable.snapTo(targetSweep)
        } else {
            // Continuous smooth sweep between one-second ticks
            sweepAnimatable.animateTo(
                targetValue = targetSweep,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
            )
        }
    }

    // Minute transition subtle pulse
    var minutePulseCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(remainingSeconds) {
        if (seconds == 0 && remainingSeconds > 0 && isRunning) {
            minutePulseCount++
        }
    }
    val pulseAnim = remember { Animatable(0f) }
    LaunchedEffect(minutePulseCount) {
        if (minutePulseCount > 0) {
            pulseAnim.snapTo(1f)
            pulseAnim.animateTo(0f, animationSpec = tween(500, easing = FastOutSlowInEasing))
        }
    }

    // Completion settle
    val completionScale by animateFloatAsState(
        targetValue = if (isCompleted) 1.03f else 1.0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "arcCompletionScale"
    )

    // Cohesive Material 3 color system
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.42f)
    val innerGuideColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.20f)

    val leadingNodeColor = if (isFinalMinute) tertiaryColor else primaryColor

    val contentDesc = CyloClockTokens.makeAccessibilityDescription(remainingSeconds, timerStatus)

    CyloClockContainer(
        modifier = modifier.semantics { contentDescription = contentDesc }
    ) { diameter ->
        val strokeWidth = (diameter.value * 0.042f).coerceIn(10f, 14f).dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = completionScale
                    scaleY = completionScale
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerOffset = Offset(size.width / 2f, size.height / 2f)
                val strokePx = strokeWidth.toPx()
                val radius = (size.minDimension / 2f) - (strokePx / 2f) - 4.dp.toPx()
                val diameterPx = radius * 2f
                val arcTopLeft = Offset(centerOffset.x - radius, centerOffset.y - radius)
                val arcSize = Size(diameterPx, diameterPx)

                // 1. Subtle Inner Quiet Boundary Guideline
                val innerGuideRadius = radius - strokePx * 1.5f
                if (innerGuideRadius > 0) {
                    drawCircle(
                        color = innerGuideColor,
                        radius = innerGuideRadius,
                        center = centerOffset,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // 2. Background Track Ring
                drawCircle(
                    color = trackColor,
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = strokePx * 0.72f)
                )

                // 3. Minute Pulse Aura (if active)
                if (pulseAnim.value > 0f) {
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.20f * pulseAnim.value),
                        radius = radius,
                        center = centerOffset,
                        style = Stroke(width = strokePx + (6.dp.toPx() * pulseAnim.value))
                    )
                }

                // 4. Primary Active Progress Arc
                val currentSweep = sweepAnimatable.value
                if (currentSweep > 0.5f) {
                    val arcBrush = Brush.sweepGradient(
                        colors = listOf(primaryColor, secondaryColor, primaryColor),
                        center = centerOffset
                    )

                    drawArc(
                        brush = arcBrush,
                        startAngle = -90f,
                        sweepAngle = currentSweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(
                            width = strokePx,
                            cap = StrokeCap.Round
                        )
                    )

                    // 5. Leading Edge Precision Marker Node
                    val currentAngleRad = ((-90f + currentSweep) * PI / 180.0).toFloat()
                    val nodeX = centerOffset.x + radius * cos(currentAngleRad)
                    val nodeY = centerOffset.y + radius * sin(currentAngleRad)

                    // Outer node halo
                    val haloAlpha = if (isPaused) 0.15f else if (isFinalMinute) 0.35f else 0.25f
                    drawCircle(
                        color = leadingNodeColor.copy(alpha = haloAlpha),
                        radius = strokePx * 0.75f,
                        center = Offset(nodeX, nodeY)
                    )
                    // Core solid bead
                    drawCircle(
                        color = leadingNodeColor,
                        radius = strokePx * 0.42f,
                        center = Offset(nodeX, nodeY)
                    )
                }
            }

            // Mathematically Centered Timer Display
            CyloTimerDisplay(
                remainingSeconds = remainingSeconds,
                timerStatus = timerStatus,
                diameter = diameter
            )
        }
    }
}
