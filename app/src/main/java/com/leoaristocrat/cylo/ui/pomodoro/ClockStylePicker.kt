package com.leoaristocrat.cylo.ui.pomodoro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leoaristocrat.cylo.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 2-Column Grid Clock Style Picker featuring real-time animated previews
 * for all 6 Cylo clock styles: Dial, Flip, Arc, Orbital, Slot Machine, and Breath.
 */
@Composable
fun ClockStylePicker(
    selectedStyleId: String,
    onSelectStyle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentStyle = ClockStyle.fromId(selectedStyleId)

    // Single shared lightweight animation transition for all preview cards
    val infiniteTransition = rememberInfiniteTransition(label = "clockPickerPreviews")

    val previewRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "previewRotation"
    )

    val previewBreath by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "previewBreath"
    )

    val previewStep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "previewStep"
    )

    val styles = ClockStyle.entries // 6 styles

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Render in symmetric pairs (3 rows of 2 cards)
        for (i in styles.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val firstStyle = styles[i]
                ClockStyleCard(
                    style = firstStyle,
                    isSelected = currentStyle == firstStyle,
                    rotation = previewRotation,
                    breath = previewBreath,
                    step = previewStep,
                    onSelect = { onSelectStyle(firstStyle.id) },
                    modifier = Modifier.weight(1f)
                )

                if (i + 1 < styles.size) {
                    val secondStyle = styles[i + 1]
                    ClockStyleCard(
                        style = secondStyle,
                        isSelected = currentStyle == secondStyle,
                        rotation = previewRotation,
                        breath = previewBreath,
                        step = previewStep,
                        onSelect = { onSelectStyle(secondStyle.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClockStyleCard(
    style: ClockStyle,
    isSelected: Boolean,
    rotation: Float,
    breath: Float,
    step: Float,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val borderColor = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val cardColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = modifier
            .semantics { role = Role.RadioButton }
            .clickable(onClick = onSelect)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top: Animated Mini Preview
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                when (style) {
                    ClockStyle.DIAL -> AnimatedDialPreview(rotation = rotation)
                    ClockStyle.FLIP -> AnimatedFlipPreview(step = step)
                    ClockStyle.ARC -> AnimatedArcPreview(step = step)
                    ClockStyle.ORBITAL -> AnimatedOrbitalPreview(rotation = rotation)
                    ClockStyle.SLOT_MACHINE -> AnimatedSlotPreview(step = step)
                    ClockStyle.BREATH -> AnimatedBreathPreview(breath = breath)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Bottom: Label + Radio Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) primaryColor else Color.Transparent)
                        .then(
                            if (!isSelected) {
                                Modifier.background(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary)
                        )
                    }
                }

                Spacer(Modifier.width(6.dp))

                Text(
                    text = style.label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun AnimatedDialPreview(rotation: Float) {
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    Canvas(modifier = Modifier.size(50.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f - 4.dp.toPx()

        drawCircle(color = track, radius = radius, center = center, style = Stroke(width = 1.5.dp.toPx()))
        drawCircle(color = track, radius = radius * 0.65f, center = center, style = Stroke(width = 1.dp.toPx()))

        for (i in 0 until 12) {
            val angleDeg = (i * 30f)
            val rad = (angleDeg * PI / 180f).toFloat()
            val startR = radius * 0.82f
            val endR = radius * 0.98f
            drawLine(
                color = track,
                start = Offset(center.x + startR * cos(rad), center.y + startR * sin(rad)),
                end = Offset(center.x + endR * cos(rad), center.y + endR * sin(rad)),
                strokeWidth = 1.5.dp.toPx()
            )
        }

        val rotRad = (rotation * PI / 180f).toFloat()
        val pillR = radius * 0.72f
        val px = center.x + pillR * cos(rotRad)
        val py = center.y + pillR * sin(rotRad)
        drawCircle(color = primary, radius = 3.5.dp.toPx(), center = Offset(px, py))
    }
}

@Composable
private fun AnimatedFlipPreview(step: Float) {
    val primary = MaterialTheme.colorScheme.primary
    val cardColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val flipAngle = (step * 180f).coerceIn(0f, 180f)

    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(cardColor),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(cardColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.Black.copy(alpha = 0.35f))
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationX = if (flipAngle > 90f) flipAngle - 180f else -flipAngle
                        cameraDistance = 12f * density
                    }
                    .background(if (flipAngle in 45f..135f) primary.copy(alpha = 0.3f) else cardColor)
            )
        }

        Text(
            text = "25",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AnimatedArcPreview(step: Float) {
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceContainerHighest

    Canvas(modifier = Modifier.size(48.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f - 4.dp.toPx()

        drawCircle(color = track, radius = radius, center = center, style = Stroke(width = 3.5.dp.toPx()))

        val sweep = (step * 360f).coerceIn(10f, 350f)
        drawArc(
            color = primary,
            startAngle = -90f,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
        )

        val endAngleRad = ((-90f + sweep) * PI / 180f).toFloat()
        val nodeX = center.x + radius * cos(endAngleRad)
        val nodeY = center.y + radius * sin(endAngleRad)
        drawCircle(color = primary, radius = 2.5.dp.toPx(), center = Offset(nodeX, nodeY))
    }
}

@Composable
private fun AnimatedOrbitalPreview(rotation: Float) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val track = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val dotColor = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = Modifier.size(48.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerR = size.minDimension / 2f - 4.dp.toPx()
        val innerR = outerR * 0.65f

        drawCircle(color = track, radius = outerR, center = center, style = Stroke(width = 1.5.dp.toPx()))
        drawCircle(color = track, radius = innerR, center = center, style = Stroke(width = 1.dp.toPx()))
        drawCircle(color = dotColor, radius = 2.dp.toPx(), center = center)

        val outerRad = (rotation * PI / 180f).toFloat()
        val ox = center.x + outerR * cos(outerRad)
        val oy = center.y + outerR * sin(outerRad)
        drawCircle(color = primary, radius = 3.5.dp.toPx(), center = Offset(ox, oy))

        val innerRad = ((rotation * 2.5f) * PI / 180f).toFloat()
        val ix = center.x + innerR * cos(innerRad)
        val iy = center.y + innerR * sin(innerRad)
        drawCircle(color = secondary, radius = 2.5.dp.toPx(), center = Offset(ix, iy))
    }
}

@Composable
private fun AnimatedSlotPreview(step: Float) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val textColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .size(width = 38.dp, height = 44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(surfaceColor),
        contentAlignment = Alignment.Center
    ) {
        val scrollY = (step * 24.dp.value)

        Text(
            text = "4",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = textColor,
            modifier = Modifier.graphicsLayer {
                translationY = scrollY * density
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to surfaceColor,
                        0.25f to Color.Transparent,
                        0.75f to Color.Transparent,
                        1.0f to surfaceColor
                    )
                )
        )
    }
}

@Composable
private fun AnimatedBreathPreview(breath: Float) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val dotColor = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = Modifier.size(50.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseRadius = 14.dp.toPx()
        val currentRadius = baseRadius * (0.85f + breath * 0.35f)

        drawCircle(
            color = primary.copy(alpha = 0.15f + breath * 0.25f),
            radius = currentRadius * 1.3f,
            center = center
        )

        drawCircle(
            color = secondary.copy(alpha = 0.3f + breath * 0.3f),
            radius = currentRadius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        drawCircle(
            color = dotColor,
            radius = 3.dp.toPx(),
            center = center
        )
    }
}
