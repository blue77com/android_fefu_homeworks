package com.example.android_fefu_homeworks.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HolidayCacheDao {
    // Возвращаем Flow для реактивного обновления UI
    @Query("SELECT * FROM holiday_cache WHERE countryCode = :countryCode AND year = :year")
    fun observeHolidays(countryCode: String, year: Int): Flow<List<HolidayCacheEntity>>

    @Query("SELECT * FROM holiday_cache WHERE countryCode = :countryCode AND year = :year")
    suspend fun getHolidaysOnce(countryCode: String, year: Int): List<HolidayCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolidays(holidays: List<HolidayCacheEntity>)

    @Query("DELETE FROM holiday_cache WHERE countryCode = :countryCode AND year = :year")
    suspend fun deleteHolidays(countryCode: String, year: Int)
}
