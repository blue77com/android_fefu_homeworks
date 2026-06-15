package com.example.android_fefu_homeworks.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class NoteOccurrenceTest {

    @Test
    fun occursOn_weeklyNote_matchesEverySevenDays() {
        val note = Note(
            id = "weekly",
            date = "2026-06-01",
            text = "Еженедельная",
            repeatMode = RepeatMode.WEEKLY,
        )

        assertTrue(note.occursOn(LocalDate.of(2026, 6, 1)))
        assertTrue(note.occursOn(LocalDate.of(2026, 6, 8)))
        assertFalse(note.occursOn(LocalDate.of(2026, 6, 9)))
    }

    @Test
    fun occursOn_yearlyNote_matchesSameDayAndMonth() {
        val note = Note(
            id = "yearly",
            date = "2024-03-15",
            text = "Ежегодная",
            repeatMode = RepeatMode.YEARLY,
        )

        assertTrue(note.occursOn(LocalDate.of(2026, 3, 15)))
        assertFalse(note.occursOn(LocalDate.of(2026, 3, 16)))
    }
}
