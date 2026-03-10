package com.example.android_fefu_homeworks.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.android_fefu_homeworks.model.Holiday

@Entity("favourite_holiday")
data class FavouriteHolidayEntity (
    @PrimaryKey
    @ColumnInfo(name = "Id") val id: String,
    val date: String,
    val localName: String,
    val name: String,
    val countryCode: String,
    val global: Boolean,
    val launchYear: Int?,
)

