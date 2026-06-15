package com.example.android_fefu_homeworks.model

import java.util.UUID

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
    val holidayId: String? = null,
    val category: NoteCategory = NoteCategory.PERSONAL,
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val checklist: List<ChecklistItem> = emptyList()
)
