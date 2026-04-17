package com.example.android_fefu_homeworks.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    "favourite_counties",
    foreignKeys = [
        ForeignKey(
            entity = FavouriteHolidayEntity::class,
            parentColumns = ["Id"],
            childColumns = ["holidayId"],
            onDelete = ForeignKey.CASCADE
        ) ]
)
data class FavouritesCountiesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "holidayId") val holidayId: String,
    val name: String,
)

data class FavoritesWithCounties(
    @Embedded val holiday: FavouriteHolidayEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "holidayId"
    )
    val Types: List<FavouritesCountiesEntity>
)