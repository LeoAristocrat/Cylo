package com.leoaristocrat.cylo.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface CyloNavKey : NavKey {
    @Serializable
    data object Focus : CyloNavKey

    @Serializable
    data object Plan : CyloNavKey

    @Serializable
    data object Analyze : CyloNavKey

    @Serializable
    data object SettingsMain : CyloNavKey

    @Serializable
    data object TimerSettings : CyloNavKey

    @Serializable
    data object AlarmSettings : CyloNavKey

    @Serializable
    data object AppearanceSettings : CyloNavKey

    @Serializable
    data object AboutSettings : CyloNavKey

    @Serializable
    data object SleepSettings : CyloNavKey

    @Serializable
    data object StepSettings : CyloNavKey

    @Serializable
    data object BackupSettings : CyloNavKey
}

typealias KimonNavKey = CyloNavKey
