package com.example.android_fefu_homeworks.data.local

import androidx.room.Embedded
import androidx.room.Relation
import com.example.android_fefu_homeworks.model.Holiday

data class FavoriteHolidayWithAll(
    @Embedded val holiday: FavouriteHolidayEntity,
    @Relation(parentColumn = "Id", entityColumn = "holidayId")
    val types: List<FavouritesTypesEntity>,
    @Relation(parentColumn = "Id", entityColumn = "holidayId")
    val counties: List<FavouritesCountiesEntity>
)

fun FavoriteHolidayWithAll.toDomain(): Holiday {
    return Holiday(
        date = holiday.date,
        localName = holiday.localName,
        name = holiday.name,
        countryCode = holiday.countryCode,
        global = holiday.global,
        types = types.map { it.name },
        counties = counties.map { it.name }
    )
}