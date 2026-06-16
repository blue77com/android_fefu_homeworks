package com.example.android_fefu_homeworks

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.android_fefu_homeworks.notification.DailyWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class HolidayApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleDailyWorker()
        if (BuildConfig.DEBUG) {
            scheduleDebugNotificationCheck()
        }
    }

    private fun scheduleDailyWorker() {
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyEventNotification",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }

    /** Только debug: запуск DailyWorker через 5 с после старта — для проверки уведомлений без Background Task Inspector. */
    private fun scheduleDebugNotificationCheck() {
        val request = OneTimeWorkRequestBuilder<DailyWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "DebugDailyNotification",
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}
