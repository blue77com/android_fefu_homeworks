package com.example.android_fefu_homeworks.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.android_fefu_homeworks.data.HolidayRepository
import com.example.android_fefu_homeworks.data.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class DailyWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: HolidayRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val year = today.year
        val allNotes = repository.observeNotes().first()

        val countryCode = settingsRepository.selectedCountryCode.first()
        val holidays = if (countryCode != null) {
            try {
                repository.refreshPublicHolidays(year, countryCode)
            } catch (_: Exception) {
            }
            try {
                repository.observePublicHolidays(year, countryCode).first()
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        val events = DailyEventFormatter.eventsForToday(
            notes = allNotes,
            holidays = holidays,
            today = today,
        )

        if (events.isNotEmpty()) {
            notificationHelper.showDailyNotification(
                title = "События на сегодня",
                message = events.joinToString("\n"),
            )
        }

        return Result.success()
    }
}
