package com.leoaristocrat.cylo.ui.pomodoro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.unit.sp
import kotlin.math.abs

/**
 * Slot Machine Clock Style: Time as a physical mechanical reel.
 * 4 precision recessed vertical apertures where only the changing reels physically roll,
 * featuring physical spring settling and cylindrical shadow curvature.
 */
@Composable
fun SlotMachinePomodoroClock(
    remainingSeconds: Int,
    totalSeconds: Int = 25 * 60,
    timerStatus: TimerStatus = TimerStatus.IDLE,
    modifier: Modifier = Modifier
) {
    val isRunning = timerStatus == TimerStatus.RUNNING
    val isPaused = timerStatus == TimerStatus.PAUSED
    val isCompleted = remainingSeconds <= 0 && timerStatus != TimerStatus.IDLE

    val safeSeconds = remainingSeconds.coerceAtLeast(0)
    val minutes = safeSeconds / 60
    val seconds = safeSeconds % 60

    val m1 = (minutes / 10).coerceIn(0, 9)
    val m2 = (minutes % 10).coerceIn(0, 9)
    val s1 = (seconds / 10).coerceIn(0, 5)
    val s2 = (seconds % 10).coerceIn(0, 9)

    val contentDesc = CyloClockTokens.makeAccessibilityDescription(remainingSeconds, timerStatus)
    val fontFamily = rememberCyloClockFontFamily()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .semantics { contentDescription = contentDesc },
        contentAlignment = Alignment.Center
    ) {
        val availableW = maxWidth
        val reelWidth = ((availableW - 56.dp) / 4.4f).coerceIn(50.dp, 76.dp)
        val reelHeight = (reelWidth * 1.52f).coerceIn(80.dp, 120.dp)
        val fontSize = (reelHeight.value * 0.54f).sp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Precision Mechanical Console Chassis
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                shadowElevation = 3.dp,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Minutes Tens Aperture
                    MechanicalSlotReel(
                        digit = m1,
                        width = reelWidth,
                        height = reelHeight,
                        fontSize = fontSize,
                        fontFamily = fontFamily
                    )

                    // Minutes Ones Aperture
                    MechanicalSlotReel(
                        digit = m2,
                        width = reelWidth,
                        height = reelHeight,
                        fontSize = fontSize,
                        fontFamily = fontFamily
                    )

                    // Center Mechanical Colon Divider
                    MechanicalColonDivider(
                        height = reelHeight,
                        isTicking = isRunning && (seconds % 2 == 0)
                    )

                    // Seconds Tens Aperture
                    MechanicalSlotReel(
                        digit = s1,
                        width = reelWidth,
                        height = reelHeight,
                        fontSize = fontSize,
                        fontFamily = fontFamily
                    )

                    // Seconds Ones Aperture
                    MechanicalSlotReel(
                        digit = s2,
                        width = reelWidth,
                        height = reelHeight,
                        fontSize = fontSize,
                        fontFamily = fontFamily
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Subtitle status badge matching Cylo design system
            Text(
                text = when {
                    isPaused -> "REELS PAUSED"
                    isCompleted -> "SESSION COMPLETE"
                    isRunning -> "FOCUS IN PROGRESS"
                    else -> "MECHANISM READY"
                },
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = CyloClockTokens.StatusFontSize,
                    letterSpacing = CyloClockTokens.StatusTrackingLetterSpacing,
                    textAlign = TextAlign.Center
                ),
                color = when {
                    isPaused -> MaterialTheme.colorScheme.tertiary
                    isCompleted -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                }
            )
        }
    }
}

/**
 * Individual physical vertical rolling reel inside an optical cylindrical aperture.
 * ONLY triggers animation when its specific digit value changes!
 */
@Composable
private fun MechanicalSlotReel(
    digit: Int,
    width: Dp,
    height: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier
) {
    var previousDigit by remember { mutableIntStateOf(digit) }
    var currentDigit by remember { mutableIntStateOf(digit) }

    val rollAnim = remember { Animatable(1f) }

    LaunchedEffect(digit) {
        if (digit != currentDigit) {
            val old = currentDigit
            val delta = abs(digit - old)
            previousDigit = old
            currentDigit = digit

            val isWrap = (old == 0 && (digit == 9 || digit == 5))
            val isSingleStep = delta == 1 || isWrap

            if (!isSingleStep) {
                // Large duration jumps / resets: snap directly without spinning intermediate numbers
                rollAnim.snapTo(1f)
            } else {
                // Real physical mechanical roll: spring with natural inertia and subtle settling
                rollAnim.snapTo(0f)
                rollAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.78f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
        }
    }

    val progress = rollAnim.value
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val textColor = MaterialTheme.colorScheme.onSurface
    val paylineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.32f)

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceColor)
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        // 1. Horizontal Center Payline Guide
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(paylineColor)
        )

        // 2. Physical Rolling Column
        val outgoingY = progress * height.value
        val incomingY = (progress - 1f) * height.value

        val digitTextStyle = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both
            )
        )

        // Outgoing numeral (descending out the bottom)
        if (progress < 1f) {
            Text(
                text = "$previousDigit",
                style = digitTextStyle,
                color = textColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = outgoingY * density
                        alpha = (1f - progress * 0.9f).coerceIn(0f, 1f)
                    }
            )
        }

        // Incoming numeral (entering from the top, settling into center)
        Text(
            text = "$currentDigit",
            style = digitTextStyle,
            color = textColor,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = if (progress < 1f) incomingY * density else 0f
                    alpha = if (progress < 1f) (0.25f + progress * 0.75f).coerceIn(0f, 1f) else 1f
                }
        )

        // 3. Top and Bottom Cylindrical Shadow Gradient Mask
        // Simulates physical curvature of cylindrical reels receding into top and bottom bezels
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to surfaceColor.copy(alpha = 0.94f),
                        0.20f to Color.Transparent,
                        0.80f to Color.Transparent,
                        1.0f to surfaceColor.copy(alpha = 0.94f)
                    )
                )
        )
    }
}

/**
 * Mechanical Colon Divider between minutes and seconds.
 */
@Composable
private fun MechanicalColonDivider(
    height: Dp,
    isTicking: Boolean,
    modifier: Modifier = Modifier
) {
    val dotAlpha by animateFloatAsState(
        targetValue = if (isTicking) 1f else 0.40f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "slotColonAlpha"
    )

    Column(
        modifier = modifier
            .height(height)
            .padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = dotAlpha))
        )
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = dotAlpha))
        )
    }
}
