package com.example.android_fefu_homeworks.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HolidayCacheDao {
    @Query("SELECT * FROM holiday_cache WHERE countryCode = :countryCode AND year = :year")
    suspend fun getHolidays(countryCode: String, year: Int): List<HolidayCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolidays(holidays: List<HolidayCacheEntity>)

    @Query("DELETE FROM holiday_cache WHERE countryCode = :countryCode AND year = :year")
    suspend fun deleteHolidays(countryCode: String, year: Int)
}
