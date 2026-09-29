package com.leoaristocrat.cylo

import android.app.Application
import com.leoaristocrat.cylo.data.local.CyloDatabase
import com.leoaristocrat.cylo.data.repository.SessionRepository
import com.leoaristocrat.cylo.data.repository.TagRepository
import com.leoaristocrat.cylo.data.repository.TaskRepository
import com.leoaristocrat.cylo.data.repository.UserSettingsRepository
import com.leoaristocrat.cylo.domain.usecase.GetDayStatsUseCase
import com.leoaristocrat.cylo.domain.usecase.GetOverviewStatsUseCase
import com.leoaristocrat.cylo.domain.usecase.GetWeekStatsUseCase
import com.leoaristocrat.cylo.domain.usecase.GetYearStatsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CyloApplication : Application() {

    val database by lazy { CyloDatabase.getInstance(this) }
    val sessionRepository by lazy { SessionRepository(database.focusSessionDao(), this) }
    val tagRepository by lazy { TagRepository(database.tagDao()) }
    val taskRepository by lazy { TaskRepository(database.taskDao()) }
    val userSettingsRepository by lazy { UserSettingsRepository(this) }

    val sleepMonitorManager by lazy { com.leoaristocrat.cylo.service.sleep.SleepMonitorManager(this) }
    val stepCounterManager by lazy { com.leoaristocrat.cylo.service.step.StepCounterManager(this) }
    val healthConnectManager by lazy { com.leoaristocrat.cylo.service.health.HealthConnectManager(this) }
    val sleepRepository by lazy {
        com.leoaristocrat.cylo.data.repository.SleepRepository(
            context = this,
            sleepSessionDao = database.sleepSessionDao(),
            sleepMonitorManager = sleepMonitorManager,
            healthConnectManager = healthConnectManager
        )
    }

    val backupRepository by lazy {
        com.leoaristocrat.cylo.data.backup.BackupRepository(
            context = this,
            database = database,
            userSettingsRepository = userSettingsRepository
        )
    }

    val getOverviewStatsUseCase by lazy { GetOverviewStatsUseCase(sessionRepository) }
    val getDayStatsUseCase by lazy { GetDayStatsUseCase(sessionRepository) }
    val getWeekStatsUseCase by lazy { GetWeekStatsUseCase(sessionRepository) }
    val getYearStatsUseCase by lazy { GetYearStatsUseCase(sessionRepository) }

    override fun onCreate() {
        super.onCreate()
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                if (userSettingsRepository.sleepMonitoringEnabled.first() && sleepMonitorManager.hasPermission()) {
                    sleepMonitorManager.startSleepMonitoring()
                }
            } catch (_: Exception) {}

            try {
                if (userSettingsRepository.stepCounterEnabled.first() && stepCounterManager.hasPermission()) {
                    com.leoaristocrat.cylo.service.step.StepCounterService.start(this@CyloApplication)
                }
            } catch (_: Exception) {}
        }
    }
}

typealias KimonApplication = CyloApplication
