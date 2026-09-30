package com.leoaristocrat.cylo.ui.pomodoro

import android.graphics.Typeface
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.leoaristocrat.cylo.R

/**
 * Shared Cylo Clock Design System.
 * Ensures mathematical centering, consistent typography, true circular geometry,
 * uniform safe bounds, and harmonious state communication across all clock styles.
 */
object CyloClockTokens {
    val MaxClockDiameter = 320.dp
    val MinClockDiameter = 220.dp
    val DefaultDiameterScale = 0.86f

    val StatusTrackingLetterSpacing = 2.4.sp
    val StatusFontSize = 11.5.sp

    fun formatTime(remainingSeconds: Int): String {
        val safeSeconds = remainingSeconds.coerceAtLeast(0)
        val m = safeSeconds / 60
        val s = safeSeconds % 60
        return String.format("%02d:%02d", m, s)
    }

    fun makeAccessibilityDescription(
        remainingSeconds: Int,
        timerStatus: TimerStatus
    ): String {
        val safeSeconds = remainingSeconds.coerceAtLeast(0)
        val m = safeSeconds / 60
        val s = safeSeconds % 60
        val statusText = when (timerStatus) {
            TimerStatus.RUNNING -> "Timer running"
            TimerStatus.PAUSED -> "Timer paused"
            TimerStatus.IDLE -> if (safeSeconds == 0) "Timer completed" else "Timer idle"
        }
        return "$m minutes $s seconds remaining, $statusText"
    }
}

/**
 * Provides the shared Google Sans Flex typography family.
 */
@Composable
fun rememberCyloClockFontFamily(): FontFamily {
    val context = LocalContext.current
    val typeface = remember(context) {
        ResourcesCompat.getFont(context, R.font.google_sans_flex) ?: Typeface.DEFAULT_BOLD
    }
    return remember(typeface) {
        FontFamily(androidx.compose.ui.text.font.Typeface(typeface))
    }
}

/**
 * Shared Clock Viewport Container.
 * Enforces true 1:1 circular/square aspect ratio, strict geometric centering,
 * and unified screen footprint so clocks never shift or jump when switching styles.
 */
@Composable
fun CyloClockContainer(
    modifier: Modifier = Modifier,
    scaleFactor: Float = CyloClockTokens.DefaultDiameterScale,
    content: @Composable BoxScope.(diameter: Dp) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        val sizePx = min(maxWidth, maxHeight)
        val diameter = (sizePx * scaleFactor).coerceIn(
            CyloClockTokens.MinClockDiameter,
            CyloClockTokens.MaxClockDiameter
        )

        Box(
            modifier = Modifier
                .size(diameter)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            content(diameter)
        }
    }
}

/**
 * Mathematically Centered Timer Display.
 * Standardizes time digits and status subtitle badge across all radial clocks.
 */
@Composable
fun CyloTimerDisplay(
    remainingSeconds: Int,
    timerStatus: TimerStatus,
    diameter: Dp,
    modifier: Modifier = Modifier,
    customStatusLabel: String? = null
) {
    val fontFamily = rememberCyloClockFontFamily()
    val timeText = remember(remainingSeconds) { CyloClockTokens.formatTime(remainingSeconds) }
    val isRunning = timerStatus == TimerStatus.RUNNING
    val isPaused = timerStatus == TimerStatus.PAUSED
    val isCompleted = remainingSeconds <= 0 && timerStatus != TimerStatus.IDLE

    val fontSize = (diameter.value * 0.20f).coerceIn(42f, 66f).sp

    val textColor = MaterialTheme.colorScheme.onSurface
    val statusColor by animateColorAsState(
        targetValue = when {
            isPaused -> MaterialTheme.colorScheme.tertiary
            isCompleted -> MaterialTheme.colorScheme.primary
            isRunning -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
        },
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "statusTextColor"
    )

    val labelText = customStatusLabel ?: when {
        isPaused -> "PAUSED"
        isCompleted -> "SESSION COMPLETE"
        isRunning -> "FOCUS"
        else -> "READY"
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 1. Primary timer digits: strictly, mathematically centered at dead center (centerX, centerY)
        Text(
            text = timeText,
            style = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                letterSpacing = (-1.2).sp,
                textAlign = TextAlign.Center,
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                ),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both
                )
            ),
            color = textColor
        )

        // 2. Status badge: anchored below center without displacing timer digits from exact geometric center
        val labelOffsetY = (fontSize.value * 0.58f).dp + 8.dp
        Text(
            text = labelText,
            style = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = CyloClockTokens.StatusFontSize,
                letterSpacing = CyloClockTokens.StatusTrackingLetterSpacing,
                textAlign = TextAlign.Center,
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                ),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both
                )
            ),
            color = statusColor,
            modifier = Modifier.graphicsLayer {
                translationY = labelOffsetY.toPx()
            }
        )
    }
}
