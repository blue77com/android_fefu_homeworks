package com.example.android_fefu_homeworks.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        NoteEntity::class,
        HolidayCacheEntity::class,
        CountryEntity::class,
    ],
    version = 7,
    exportSchema = false,
)
@TypeConverters(NoteConverters::class)
abstract class HolidayDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun holidayCacheDao(): HolidayCacheDao
    abstract fun countryDao(): CountryDao
}
