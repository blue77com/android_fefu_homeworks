package com.example.android_fefu_homeworks.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.abs

enum class NoteCategory(val displayName: String, val colorHex: Long) {
    PERSONAL("Личное", 0xFF9C27B0),
    WORK("Работа", 0xFF2196F3),
    HOLIDAYS("Праздники", 0xFFFF9800),
    DEADLINE("Дедлайн", 0xFFF44336)
}

enum class RepeatMode(val displayName: String) {
    NONE("Не повторять"),
    WEEKLY("Каждую неделю"),
    MONTHLY("Каждый месяц"),
    YEARLY("Каждый год")
}

data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isChecked: Boolean = false
)

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val date: String, // YYYY-MM-DD
    val text: String,
    val description: String = "",
    val isFavourite: Boolean = false,
    val category: NoteCategory = NoteCategory.PERSONAL,
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val checklist: List<ChecklistItem> = emptyList()
)

fun Note.occursOn(currentDate: LocalDate): Boolean {
    val startDate = try {
        LocalDate.parse(date)
    } catch (_: Exception) {
        return false
    }
    if (date == currentDate.toString()) return true
    return when (repeatMode) {
        RepeatMode.NONE -> false
        RepeatMode.WEEKLY -> abs(ChronoUnit.DAYS.between(startDate, currentDate)) % 7 == 0L
        RepeatMode.MONTHLY -> currentDate.dayOfMonth == startDate.dayOfMonth
        RepeatMode.YEARLY ->
            currentDate.dayOfMonth == startDate.dayOfMonth && currentDate.month == startDate.month
    }
}
