package com.example.android_fefu_homeworks.notification

import com.example.android_fefu_homeworks.model.Holiday
import com.example.android_fefu_homeworks.model.Note
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DailyEventFormatterTest {

    @Test
    fun eventsForToday_includesNotesAndHolidaysForDate() {
        val today = LocalDate.of(2026, 6, 15)
        val notes = listOf(
            Note(id = "n1", date = "2026-06-15", text = "Встреча", isFavourite = true),
        )
        val holidays = listOf(
            holiday(date = "2026-06-15", localName = "День России"),
        )

        val events = DailyEventFormatter.eventsForToday(notes, holidays, today)

        assertEquals(2, events.size)
        assertTrue(events.any { it.contains("Избранная заметка") && it.contains("Встреча") })
        assertTrue(events.any { it.contains("Праздник: День России") })
    }

    @Test
    fun eventsForToday_whenNoMatches_returnsEmptyList() {
        val today = LocalDate.of(2026, 6, 15)

        val events = DailyEventFormatter.eventsForToday(
            notes = listOf(Note(id = "n1", date = "2026-07-01", text = "Позже")),
            holidays = listOf(holiday(date = "2026-01-01", localName = "Новый год")),
            today = today,
        )

        assertTrue(events.isEmpty())
    }

    private fun holiday(date: String, localName: String): Holiday = Holiday(
        date = date,
        localName = localName,
        name = localName,
        countryCode = "RU",
        global = true,
        counties = null,
        types = listOf("Public"),
    )
}
