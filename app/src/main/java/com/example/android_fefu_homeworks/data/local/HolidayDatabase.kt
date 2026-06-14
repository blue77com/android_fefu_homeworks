package com.example.android_fefu_homeworks.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavouriteHolidayEntity::class,
        FavouritesTypesEntity::class,
        FavouritesCountiesEntity::class,
        NoteEntity::class,
        HolidayCacheEntity::class,
        CountryEntity::class
    ],
    version = 4,
)
abstract class HolidayDatabase : RoomDatabase() {
    abstract fun favoriteHolidayDao(): FavoriteHolidayDao
    abstract fun noteDao(): NoteDao
    abstract fun holidayCacheDao(): HolidayCacheDao
    abstract fun countryDao(): CountryDao
}