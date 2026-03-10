package com.example.android_fefu_homeworks.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavouriteHolidayEntity::class,
        FavouritesTypesEntity::class,
        FavouritesCountiesEntity::class],
    version = 1,
)

abstract class HolidayDatabase : RoomDatabase() {
    abstract fun favoriteHolidayDao(): FavoriteHolidayDao
}