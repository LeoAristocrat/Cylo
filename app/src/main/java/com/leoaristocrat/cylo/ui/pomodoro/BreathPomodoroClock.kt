package com.leoaristocrat.cylo.ui.pomodoro

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Breath Clock Style: Time as rhythm.
 * An ambient, calm focus aura expanding and contracting in a relaxed 8-second breathing cycle,
 * completely decoupled from the 1-second countdown tick, with layered harmonic contours.
 */
@Composable
fun BreathPomodoroClock(
    remainingSeconds: Int,
    totalSeconds: Int = 25 * 60,
    timerStatus: TimerStatus = TimerStatus.IDLE,
    modifier: Modifier = Modifier
) {
    val isRunning = timerStatus == TimerStatus.RUNNING
    val isPaused = timerStatus == TimerStatus.PAUSED
    val isCompleted = remainingSeconds <= 0 && timerStatus != TimerStatus.IDLE

    // Slow, soothing 8-second continuous breathing cycle (4s expand + 4s contract)
    val infiniteTransition = rememberInfiniteTransition(label = "breathCycleTransition")
    val rawBreathScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4000,
                easing = CubicBezierEasing(0.42f, 0.0f, 0.58f, 1.0f)
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rawBreathScale"
    )

    // When paused or idle, gently ease the breathing scale to 1.0f baseline
    val activeBreathScale by animateFloatAsState(
        targetValue = if (isRunning) rawBreathScale else 1.0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "activeBreathScale"
    )

    // Restrained atmospheric opacity
    val auraAlpha by animateFloatAsState(
        targetValue = when {
            isRunning -> 0.22f + ((rawBreathScale - 0.90f) / 0.22f) * 0.14f
            isPaused -> 0.12f
            isCompleted -> 0.28f
            else -> 0.15f
        },
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "breathAuraAlpha"
    )

    // Completion settle
    val completionScale by animateFloatAsState(
        targetValue = if (isCompleted) 1.03f else 1.0f,
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label = "breathCompletionScale"
    )

    // Material 3 colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val restingHorizonColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)

    val contentDesc = CyloClockTokens.makeAccessibilityDescription(remainingSeconds, timerStatus)

    CyloClockContainer(
        modifier = modifier.semantics { contentDescription = contentDesc }
    ) { diameter ->
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
                val baseRadius = (size.minDimension / 2f) * 0.78f
                val dynamicRadius = baseRadius * activeBreathScale

                // 1. Outermost Atmospheric Radial Aura (Soft, restrained, non-distracting)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = auraAlpha * 0.50f),
                            secondaryColor.copy(alpha = auraAlpha * 0.18f),
                            Color.Transparent
                        ),
                        center = centerOffset,
                        radius = dynamicRadius * 1.28f
                    ),
                    radius = dynamicRadius * 1.28f,
                    center = centerOffset
                )

                // 2. Middle Harmonic Breathing Wave
                drawCircle(
                    color = primaryColor.copy(alpha = auraAlpha * 0.65f),
                    radius = dynamicRadius,
                    center = centerOffset,
                    style = Stroke(width = 2.4.dp.toPx())
                )

                // 3. Inner Resonance Contour
                drawCircle(
                    color = tertiaryColor.copy(alpha = auraAlpha * 0.75f),
                    radius = dynamicRadius * 0.84f,
                    center = centerOffset,
                    style = Stroke(width = 1.4.dp.toPx())
                )

                // 4. Stable Resting Horizon (Quiet anchor that never wavers)
                drawCircle(
                    color = restingHorizonColor,
                    radius = baseRadius * 0.68f,
                    center = centerOffset,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Mathematically Centered Timer Display
            CyloTimerDisplay(
                remainingSeconds = remainingSeconds,
                timerStatus = timerStatus,
                diameter = diameter,
                customStatusLabel = when {
                    isPaused -> "BREATH PAUSED"
                    isCompleted -> "SESSION COMPLETE"
                    isRunning -> if (rawBreathScale > 1.01f) "EXPAND" else "CONTRACT"
                    else -> "BREATHE"
                }
            )
        }
    }
}
