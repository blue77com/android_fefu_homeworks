package com.example.android_fefu_homeworks.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android_fefu_homeworks.model.Holiday

@Entity(tableName = "holiday_cache")
data class HolidayCacheEntity(
    @PrimaryKey val cacheId: String,
    val date: String,
    val localName: String,
    val name: String,
    val countryCode: String,
    val year: Int,
    val global: Boolean,
    val types: String // Сохраняем типы как строку через запятую
)

fun Holiday.toCacheEntity(year: Int) = HolidayCacheEntity(
    cacheId = "${countryCode}_${year}_${date}_${name}",
    date = date,
    localName = localName,
    name = name,
    countryCode = countryCode,
    year = year,
    global = global,
    types = types.joinToString(",")
)

fun HolidayCacheEntity.toDomain() = Holiday(
    date = date,
    localName = localName,
    name = name,
    countryCode = countryCode,
    global = global,
    counties = null,
    types = if (types.isBlank()) emptyList() else types.split(",")
)
