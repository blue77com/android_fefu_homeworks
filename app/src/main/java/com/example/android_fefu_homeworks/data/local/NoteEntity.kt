package com.example.android_fefu_homeworks.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.android_fefu_homeworks.model.ChecklistItem
import com.example.android_fefu_homeworks.model.Note
import com.example.android_fefu_homeworks.model.NoteCategory
import com.example.android_fefu_homeworks.model.RepeatMode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String,
    val date: String,
    val text: String,
    val description: String,
    val isFavourite: Boolean,
    val holidayId: String? = null,
    val category: String,
    val repeatMode: String,
    val checklistJson: String
)

class NoteConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromChecklist(list: List<ChecklistItem>): String = gson.toJson(list)

    @TypeConverter
    fun toChecklist(json: String): List<ChecklistItem> {
        val type = object : TypeToken<List<ChecklistItem>>() {}.type
        return gson.fromJson(json, type)
    }
}

fun NoteEntity.toDomain() = Note(
    id = id,
    date = date,
    text = text,
    description = description,
    isFavourite = isFavourite,
    holidayId = holidayId,
    category = NoteCategory.valueOf(category),
    repeatMode = RepeatMode.valueOf(repeatMode),
    checklist = NoteConverters().toChecklist(checklistJson)
)

fun Note.toEntity() = NoteEntity(
    id = id,
    date = date,
    text = text,
    description = description,
    isFavourite = isFavourite,
    holidayId = holidayId,
    category = category.name,
    repeatMode = repeatMode.name,
    checklistJson = NoteConverters().fromChecklist(checklist)
)
