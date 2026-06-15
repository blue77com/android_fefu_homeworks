package com.example.android_fefu_homeworks.notification

import com.example.android_fefu_homeworks.model.Holiday
import com.example.android_fefu_homeworks.model.Note
import com.example.android_fefu_homeworks.model.occursOn
import java.time.LocalDate

object DailyEventFormatter {

    fun eventsForToday(
        notes: List<Note>,
        holidays: List<Holiday>,
        today: LocalDate,
    ): List<String> {
        val todayStr = today.toString()
        val events = mutableListOf<String>()

        notes
            .filter { it.occursOn(today) }
            .forEach { note ->
                val prefix = if (note.isFavourite) "Избранная заметка" else "Заметка"
                events.add("$prefix: ${note.text}")
            }

        holidays
            .filter { it.date == todayStr }
            .forEach { events.add("Праздник: ${it.localName}") }

        return events
    }
}
