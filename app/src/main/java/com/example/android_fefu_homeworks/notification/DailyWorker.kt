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

        // 1. Проверяем личные заметки
        val allNotes = repository.observeNotes().first()
        val todayNotes = allNotes.filter { it.date == todayStr }
        todayNotes.forEach { events.add("📌 Заметка: ${it.text}") }

        // 2. Проверяем праздники для выбранной страны
        val countryCode = settingsRepository.selectedCountryCode.first()
        if (countryCode != null) {
            try {
                // Пытаемся получить праздники (репозиторий сам заберет из кэша, если они там есть)
                val holidays = repository.getPublicHolidays(year, countryCode, forceRefresh = false)
                val todayHolidays = holidays.filter { it.date == todayStr }
                todayHolidays.forEach { events.add("🎉 Праздник: ${it.localName}") }
            } catch (e: Exception) {
                // Если произошла ошибка (нет сети и нет кэша), просто пропускаем этот этап
            }
        }

        // 3. Проверяем избранное (на случай если оно не совпадает с текущей страной)
        val favourites = repository.observeFavourites().first()
        val todayFavs = favourites.filter { it.date == todayStr }
        todayFavs.forEach { fav ->
            val alreadyAdded = events.any { it.contains(fav.localName) }
            if (!alreadyAdded) {
                events.add("⭐ Избранное: ${fav.localName}")
            }
        }

        if (events.isNotEmpty()) {
            notificationHelper.showDailyNotification(
                title = "События на сегодня",
                message = events.joinToString("\n")
            )
        }

        return Result.success()
    }
}
