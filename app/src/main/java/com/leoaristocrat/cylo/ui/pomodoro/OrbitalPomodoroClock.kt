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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Orbital Clock Style: Time as coordinated motion.
 * Concentric orbits where outer orbit traces overall session progress with a comet trail,
 * and inner orbit tracks second precision, revolving around an unobstructed center timer.
 */
@Composable
fun OrbitalPomodoroClock(
    remainingSeconds: Int,
    totalSeconds: Int = 25 * 60,
    timerStatus: TimerStatus = TimerStatus.IDLE,
    modifier: Modifier = Modifier
) {
    val isRunning = timerStatus == TimerStatus.RUNNING
    val isPaused = timerStatus == TimerStatus.PAUSED
    val isCompleted = remainingSeconds <= 0 && timerStatus != TimerStatus.IDLE
    val seconds = remainingSeconds % 60

    // Elapsed session progress (0.0 to 1.0)
    val elapsedFraction = if (totalSeconds > 0) {
        ((totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    // 1. Outer Orbit Angle (Overall session journey from -90° to +270°)
    val targetOuterAngle = -90f + (elapsedFraction * 360f)

    // 2. Inner Orbit Angle (Seconds satellite 0 to 59s -> 0° to 360°)
    val targetInnerAngle = -90f + (((60 - seconds) % 60).toFloat() / 60f * 360f)

    val outerAngleAnim = remember { Animatable(targetOuterAngle) }
    val innerAngleAnim = remember { Animatable(targetInnerAngle) }

    // Smooth outer orbit animation
    LaunchedEffect(targetOuterAngle, isRunning) {
        val diff = abs(targetOuterAngle - outerAngleAnim.value)
        if (diff > 25f || !isRunning) {
            outerAngleAnim.snapTo(targetOuterAngle)
        } else {
            outerAngleAnim.animateTo(
                targetValue = targetOuterAngle,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
            )
        }
    }

    // Smooth inner orbit animation (seconds satellite)
    LaunchedEffect(targetInnerAngle, isRunning) {
        val diff = abs(targetInnerAngle - innerAngleAnim.value)
        if (diff > 35f || !isRunning) {
            innerAngleAnim.snapTo(targetInnerAngle)
        } else {
            innerAngleAnim.animateTo(
                targetValue = targetInnerAngle,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
            )
        }
    }

    // Completion convergence animation: Satellites gently settle at zenith (-90°)
    val convergenceProgress by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "orbitalConvergence"
    )

    // Theme color palette
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)
    val hubBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.16f)

    val contentDesc = CyloClockTokens.makeAccessibilityDescription(remainingSeconds, timerStatus)

    CyloClockContainer(
        modifier = modifier.semantics { contentDescription = contentDesc }
    ) { diameter ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerOffset = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = (size.minDimension / 2f) - 8.dp.toPx()

                // Dual concentric orbits with strict geometric hierarchy
                val baseOuterRadius = baseRadius * 0.94f
                val baseInnerRadius = baseRadius * 0.72f
                val hubRadius = baseRadius * 0.52f

                // During completion, orbits softly draw in toward resting alignment
                val outerRadius = baseOuterRadius * (1f - convergenceProgress * 0.12f)
                val innerRadius = baseInnerRadius * (1f - convergenceProgress * 0.16f)

                // 1. Center Protected Hub Guideline (Guarantees satellites never enter typography area)
                drawCircle(
                    color = hubBorderColor,
                    radius = hubRadius,
                    center = centerOffset,
                    style = Stroke(width = 1.dp.toPx())
                )

                // 2. Inner Orbit Track (Dashed rhythm ring for seconds satellite)
                val dashPattern = PathEffect.dashPathEffect(
                    floatArrayOf(5.dp.toPx(), 6.dp.toPx()),
                    0f
                )
                drawCircle(
                    color = trackColor.copy(alpha = 0.24f),
                    radius = innerRadius,
                    center = centerOffset,
                    style = Stroke(width = 1.2.dp.toPx(), pathEffect = dashPattern)
                )

                // 3. Outer Orbit Track (Continuous hairline for macro session journey)
                drawCircle(
                    color = trackColor,
                    radius = outerRadius,
                    center = centerOffset,
                    style = Stroke(width = 1.6.dp.toPx())
                )

                // 4. Coordinates Calculation
                val currentOuterAngle = if (convergenceProgress > 0f) {
                    outerAngleAnim.value * (1f - convergenceProgress) + (-90f * convergenceProgress)
                } else outerAngleAnim.value

                val currentInnerAngle = if (convergenceProgress > 0f) {
                    innerAngleAnim.value * (1f - convergenceProgress) + (-90f * convergenceProgress)
                } else innerAngleAnim.value

                val outerRad = (currentOuterAngle * PI / 180.0).toFloat()
                val outerX = centerOffset.x + outerRadius * cos(outerRad)
                val outerY = centerOffset.y + outerRadius * sin(outerRad)

                val innerRad = (currentInnerAngle * PI / 180.0).toFloat()
                val innerX = centerOffset.x + innerRadius * cos(innerRad)
                val innerY = centerOffset.y + innerRadius * sin(innerRad)

                // 5. Outer Orbit Elapsed Comet Trail (Draws trajectory arc behind outer node)
                val trailSpanDeg = (elapsedFraction * 360f).coerceIn(0f, 360f)
                if (trailSpanDeg > 1f && convergenceProgress < 1f) {
                    val trailTopLeft = Offset(centerOffset.x - outerRadius, centerOffset.y - outerRadius)
                    val trailSize = Size(outerRadius * 2f, outerRadius * 2f)
                    drawArc(
                        color = primaryColor.copy(alpha = if (isPaused) 0.18f else 0.40f),
                        startAngle = -90f,
                        sweepAngle = trailSpanDeg,
                        useCenter = false,
                        topLeft = trailTopLeft,
                        size = trailSize,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // 6. Draw Outer Satellite Node (Macro Progress Node)
                val outerNodeRadius = 6.dp.toPx()
                // Outer glow aura
                drawCircle(
                    color = primaryColor.copy(alpha = if (isPaused) 0.15f else 0.30f),
                    radius = outerNodeRadius + 4.dp.toPx(),
                    center = Offset(outerX, outerY)
                )
                // Solid node core
                drawCircle(
                    color = primaryColor,
                    radius = outerNodeRadius,
                    center = Offset(outerX, outerY)
                )

                // 7. Draw Inner Satellite Node (Second Precision Satellite)
                val innerNodeRadius = 4.2.dp.toPx()
                // Secondary glow aura
                drawCircle(
                    color = secondaryColor.copy(alpha = if (isPaused) 0.15f else 0.28f),
                    radius = innerNodeRadius + 3.dp.toPx(),
                    center = Offset(innerX, innerY)
                )
                // Solid satellite bead
                drawCircle(
                    color = secondaryColor,
                    radius = innerNodeRadius,
                    center = Offset(innerX, innerY)
                )

                // 8. If Completed, subtle central resting aura
                if (convergenceProgress > 0f) {
                    drawCircle(
                        color = tertiaryColor.copy(alpha = 0.18f * (1f - convergenceProgress)),
                        radius = hubRadius + (convergenceProgress * 18.dp.toPx()),
                        center = centerOffset
                    )
                }
            }

            // Unobstructed Mathematically Centered Time Display
            CyloTimerDisplay(
                remainingSeconds = remainingSeconds,
                timerStatus = timerStatus,
                diameter = diameter,
                customStatusLabel = when {
                    isPaused -> "ORBIT PAUSED"
                    isCompleted -> "SESSION COMPLETE"
                    isRunning -> "ORBITING"
                    else -> "READY"
                }
            )
        }
    }
}
