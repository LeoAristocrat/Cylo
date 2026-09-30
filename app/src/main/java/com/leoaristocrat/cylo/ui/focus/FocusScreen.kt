@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.leoaristocrat.cylo.ui.focus

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leoaristocrat.cylo.R
import com.leoaristocrat.cylo.data.local.entity.TagEntity
import com.leoaristocrat.cylo.ui.pomodoro.ArcPomodoroClock
import com.leoaristocrat.cylo.ui.pomodoro.BreathPomodoroClock
import com.leoaristocrat.cylo.ui.pomodoro.ClockStyle
import com.leoaristocrat.cylo.ui.pomodoro.ConcentricPomodoroDial
import com.leoaristocrat.cylo.ui.pomodoro.FlipCardPomodoroClock
import com.leoaristocrat.cylo.ui.pomodoro.OrbitalPomodoroClock
import com.leoaristocrat.cylo.ui.pomodoro.SlotMachinePomodoroClock
import com.leoaristocrat.cylo.ui.pomodoro.TimerStatus

@Composable
fun FocusScreen(
    remainingSeconds: Int,
    totalSeconds: Int = 25 * 60,
    timerStatus: TimerStatus,
    clockStyle: String = "DIAL",
    dialTickAnimation: Boolean = false,
    selectedTag: TagEntity? = null,
    tags: List<TagEntity> = emptyList(),
    onSelectTag: (TagEntity?) -> Unit = {},
    onCreateTag: (name: String, colorHex: String) -> Unit = { _, _ -> },
    onDeleteTag: (TagEntity) -> Unit = {},
    onStart: () -> Unit,
    onPause: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRunning = timerStatus == TimerStatus.RUNNING
    val isPaused = timerStatus == TimerStatus.PAUSED
    val isTimerActive = timerStatus != TimerStatus.IDLE
    var isTagSheetOpen by remember { mutableStateOf(false) }
    val tagSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val buttonCornerRadius by animateDpAsState(
        targetValue = when {
            isRunning -> 16.dp
            isPaused -> 24.dp
            else -> 28.dp
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "actionButtonShape"
    )

    val resolvedClockStyle = remember(clockStyle) { ClockStyle.fromId(clockStyle) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 1. Clock Face (Unified Container across all 6 styles with subtle Crossfade)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = resolvedClockStyle,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(280, easing = FastOutSlowInEasing)) togetherWith
                        fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing))
                    },
                    label = "clockStyleTransition",
                    contentAlignment = Alignment.Center
                ) { targetStyle ->
                    when (targetStyle) {
                        ClockStyle.DIAL -> ConcentricPomodoroDial(
                            remainingSeconds = remainingSeconds,
                            isTickAnimation = dialTickAnimation
                        )
                        ClockStyle.FLIP -> FlipCardPomodoroClock(
                            remainingSeconds = remainingSeconds
                        )
                        ClockStyle.ARC -> ArcPomodoroClock(
                            remainingSeconds = remainingSeconds,
                            totalSeconds = totalSeconds,
                            timerStatus = timerStatus
                        )
                        ClockStyle.ORBITAL -> OrbitalPomodoroClock(
                            remainingSeconds = remainingSeconds,
                            totalSeconds = totalSeconds,
                            timerStatus = timerStatus
                        )
                        ClockStyle.SLOT_MACHINE -> SlotMachinePomodoroClock(
                            remainingSeconds = remainingSeconds,
                            totalSeconds = totalSeconds,
                            timerStatus = timerStatus
                        )
                        ClockStyle.BREATH -> BreathPomodoroClock(
                            remainingSeconds = remainingSeconds,
                            totalSeconds = totalSeconds,
                            timerStatus = timerStatus
                        )
                    }
                }
            }

            // Consistent Tag complication pill situated cleanly below ALL clocks
            FocusTagComplicationPill(
                selectedTag = selectedTag,
                onClick = { isTagSheetOpen = true },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // 3. 3-State Expressive Button (Start / Pause / Resume) + Animated Circle Restart Button
        Row(
            modifier = Modifier
                .padding(bottom = 20.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val buttonContainerColor by animateColorAsState(
                targetValue = if (isRunning) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary,
                animationSpec = tween(durationMillis = 200),
                label = "primaryButtonBg"
            )
            val buttonContentColor by animateColorAsState(
                targetValue = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                animationSpec = tween(durationMillis = 200),
                label = "primaryButtonContent"
            )

            // Primary Action Button (Start / Pause / Resume)
            Button(
                onClick = if (isRunning) onPause else onStart,
                shape = RoundedCornerShape(buttonCornerRadius),
                border = if (isRunning) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonContainerColor,
                    contentColor = buttonContentColor
                ),
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
                modifier = Modifier.height(52.dp)
            ) {
                AnimatedContent(
                    targetState = Triple(isRunning, isPaused, buttonContentColor),
                    transitionSpec = {
                        fadeIn(tween(150)) togetherWith fadeOut(tween(100))
                    },
                    label = "buttonContentTransition"
                ) { (running, paused, contentColor) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(if (running) R.drawable.ic_pause else R.drawable.ic_start),
                            contentDescription = if (running) {
                                stringResource(R.string.timer_pause)
                            } else if (paused) {
                                stringResource(R.string.timer_resume)
                            } else {
                                stringResource(R.string.timer_start)
                            },
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (running) {
                                stringResource(R.string.timer_pause)
                            } else if (paused) {
                                stringResource(R.string.timer_resume)
                            } else {
                                stringResource(R.string.timer_start)
                            },
                            color = contentColor,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            // Animated Circular Restart Button (Visible when Running or Paused)
            AnimatedVisibility(
                visible = isTimerActive,
                enter = fadeIn(animationSpec = tween(180)) +
                        scaleIn(
                            initialScale = 0.6f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ) +
                        expandHorizontally(
                            expandFrom = Alignment.Start,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        ),
                exit = fadeOut(animationSpec = tween(120)) +
                        scaleOut(
                            targetScale = 0.6f,
                            animationSpec = tween(120)
                        ) +
                        shrinkHorizontally(
                            shrinkTowards = Alignment.Start,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(14.dp))
                    FilledTonalIconButton(
                        onClick = onRestart,
                        shape = CircleShape,
                        modifier = Modifier.size(52.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_restart),
                            contentDescription = stringResource(R.string.timer_restart),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }

    // 4. Tag Selection BottomSheet
    if (isTagSheetOpen) {
        TagSelectionBottomSheet(
            sheetState = tagSheetState,
            tags = tags,
            selectedTag = selectedTag,
            onSelectTag = onSelectTag,
            onCreateTag = onCreateTag,
            onDeleteTag = onDeleteTag,
            onDismissRequest = { isTagSheetOpen = false }
        )
    }
}

@Composable
private fun FocusTagComplicationPill(
    selectedTag: TagEntity?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            if (selectedTag != null) {
                val tagColor = remember(selectedTag.colorHex) {
                    runCatching { Color(android.graphics.Color.parseColor(selectedTag.colorHex)) }.getOrNull()
                } ?: MaterialTheme.colorScheme.primary
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = tagColor,
                            shape = CircleShape
                        )
                )
                Text(
                    text = selectedTag.name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_tag),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = stringResource(R.string.tag_choose_button),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
