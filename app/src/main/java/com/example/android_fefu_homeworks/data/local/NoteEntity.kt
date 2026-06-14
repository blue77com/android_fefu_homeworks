package com.example.android_fefu_homeworks.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android_fefu_homeworks.model.Note

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String,
    val date: String,
    val text: String,
    val holidayId: String? = null
)

fun NoteEntity.toDomain() = Note(
    id = id,
    date = date,
    text = text,
    holidayId = holidayId
)

fun Note.toEntity() = NoteEntity(
    id = id,
    date = date,
    text = text,
    holidayId = holidayId
)
