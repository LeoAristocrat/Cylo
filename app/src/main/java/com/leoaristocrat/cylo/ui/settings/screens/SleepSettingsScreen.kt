@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.leoaristocrat.cylo.ui.settings.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.leoaristocrat.cylo.CyloApplication
import com.leoaristocrat.cylo.R
import com.leoaristocrat.cylo.data.repository.UserSettingsRepository
import com.leoaristocrat.cylo.service.health.HealthConnectManager
import com.leoaristocrat.cylo.ui.components.bouncyScroll
import com.leoaristocrat.cylo.ui.settings.SettingsUiState
import com.leoaristocrat.cylo.ui.sleep.SleepViewModel
import com.leoaristocrat.cylo.ui.theme.CustomColors.cardBorder
import com.leoaristocrat.cylo.ui.theme.CustomColors.cardContainerColor
import com.leoaristocrat.cylo.ui.theme.CustomColors.listItemColors
import com.leoaristocrat.cylo.ui.theme.CustomColors.switchColors
import com.leoaristocrat.cylo.ui.theme.CustomColors.topBarColors
import com.leoaristocrat.cylo.ui.theme.CyloShapeDefaults.cardShape
import com.leoaristocrat.cylo.ui.theme.CyloShapeDefaults.segmentedListItemShapes
import com.leoaristocrat.cylo.ui.theme.LocalAppFonts
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SleepSettingsScreen(
    state: SettingsUiState,
    onToggleSleepMonitoring: (Boolean) -> Unit,
    onToggleHealthConnectSync: (Boolean) -> Unit,
    onSetSleepGoal: (Int) -> Unit,
    onToggleScheduledMode: (Boolean) -> Unit = {},
    onSetBedtime: (Int, Int) -> Unit = { _, _ -> },
    onSetWakeTime: (Int, Int) -> Unit = { _, _ -> },
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cyloApp = context.applicationContext as CyloApplication
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val healthConnectPermissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        if (grantedPermissions.containsAll(HealthConnectManager.PERMISSIONS)) {
            onToggleHealthConnectSync(true)
            coroutineScope.launch {
                cyloApp.sleepRepository.syncFromHealthConnect()
                cyloApp.sleepRepository.syncUnsyncedToHealthConnect()
            }
        } else {
            Toast.makeText(context, "Health Connect permissions were not fully granted", Toast.LENGTH_SHORT).show()
            onToggleHealthConnectSync(false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_section_sleep),
                        fontFamily = LocalAppFonts.current.topBarTitle,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = listItemColors.containerColor
                        ),
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_left),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = topBarColors
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .bouncyScroll()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Spacer(Modifier.height(6.dp))
            // ==========================================
            // CATEGORY 1: SCHEDULE & TARGET GOALS (MONOCHROME)
            // ==========================================
            Text(
                text = stringResource(R.string.settings_section_sleep_goal),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
            )

            // 1. Daily Sleep Goal Stepper
            SegmentedListItem(
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_bed),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                trailingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (state.sleepGoalMinutes > 240) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSetSleepGoal(state.sleepGoalMinutes - 30)
                                }
                            },
                            enabled = state.sleepGoalMinutes > 240,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_remove),
                                contentDescription = "Decrease",
                                modifier = Modifier.size(16.dp),
                                tint = if (state.sleepGoalMinutes > 240) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
                            )
                        }

                        Text(
                            text = SleepViewModel.formatDuration(state.sleepGoalMinutes.toLong()),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                fontFamily = LocalAppFonts.current.topBarTitle
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        IconButton(
                            onClick = {
                                if (state.sleepGoalMinutes < 720) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSetSleepGoal(state.sleepGoalMinutes + 30)
                                }
                            },
                            enabled = state.sleepGoalMinutes < 720,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_add),
                                contentDescription = "Increase",
                                modifier = Modifier.size(16.dp),
                                tint = if (state.sleepGoalMinutes < 720) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                },
                shapes = segmentedListItemShapes(0, 1),
                colors = listItemColors,
                onClick = {}
            ) {
                Text(
                    text = stringResource(R.string.settings_sleep_goal),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(14.dp))

            // ==========================================
            // CATEGORY 2: SYNC & PHONE TRACKING (MONOCHROME)
            // ==========================================
            Text(
                text = stringResource(R.string.settings_section_sleep_tracking_sync),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
            )

            // 1. Health Connect Sync Switch
            SegmentedListItem(
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_sparkles),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                trailingContent = {
                    Switch(
                        checked = state.healthConnectSyncEnabled,
                        onCheckedChange = null,
                        thumbContent = {
                            if (state.healthConnectSyncEnabled) {
                                Icon(
                                    painter = painterResource(R.drawable.check),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            } else {
                                Icon(
                                    painter = painterResource(R.drawable.clear),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        },
                        colors = switchColors
                    )
                },
                shapes = segmentedListItemShapes(0, 1),
                colors = listItemColors,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val target = !state.healthConnectSyncEnabled
                    if (target) {
                        if (!cyloApp.healthConnectManager.isAvailable()) {
                            Toast.makeText(context, "Health Connect is not available on this device", Toast.LENGTH_LONG).show()
                            onToggleHealthConnectSync(false)
                        } else {
                            healthConnectPermissionLauncher.launch(HealthConnectManager.PERMISSIONS)
                        }
                    } else {
                        onToggleHealthConnectSync(false)
                    }
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.settings_health_connect_sync),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.settings_health_connect_sync_desc),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.5.sp,
                            lineHeight = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2. Sync Data Now (Small Primary Container Pill Button)
            if (state.healthConnectSyncEnabled) {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            coroutineScope.launch {
                                cyloApp.sleepRepository.syncFromHealthConnect()
                                cyloApp.sleepRepository.syncUnsyncedToHealthConnect()
                                Toast.makeText(context, context.getString(R.string.settings_sync_success), Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_sparkles),
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = stringResource(R.string.settings_sync_now),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ==========================================
            // INFO CARD (PRIVACY & METHOD - MONOCHROME)
            // ==========================================
            Surface(
                shape = cardShape,
                color = cardContainerColor,
                border = cardBorder,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_moon),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = stringResource(R.string.settings_sleep_info_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = stringResource(R.string.settings_sleep_info_desc),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}
