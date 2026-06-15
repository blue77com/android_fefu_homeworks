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
        val todayStr = today.toString()
        val year = today.year
        val events = mutableListOf<String>()

        val allNotes = repository.observeNotes().first()
        allNotes
            .filter { it.date == todayStr }
            .forEach { note ->
                val prefix = if (note.isFavourite) "Избранная заметка" else "Заметка"
                events.add("$prefix: ${note.text}")
            }

        val countryCode = settingsRepository.selectedCountryCode.first()
        if (countryCode != null) {
            try {
                repository.refreshPublicHolidays(year, countryCode)
            } catch (_: Exception) {
            }

            try {
                val holidays = repository.observePublicHolidays(year, countryCode).first()
                holidays
                    .filter { it.date == todayStr }
                    .forEach { events.add("Праздник: ${it.localName}") }
            } catch (_: Exception) {
            }
        }

        if (events.isNotEmpty()) {
            notificationHelper.showDailyNotification(
                title = "События на сегодня",
                message = events.joinToString("\n"),
            )
        }

        return Result.success()
    }
}
