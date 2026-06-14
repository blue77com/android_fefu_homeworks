package com.example.android_fefu_homeworks.model

import java.util.UUID

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val date: String, // YYYY-MM-DD
    val text: String,
    val description: String = "",
    val isFavourite: Boolean = false,
    val holidayId: String? = null
)
