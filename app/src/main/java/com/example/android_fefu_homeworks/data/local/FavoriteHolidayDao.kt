package com.example.android_fefu_homeworks.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow


@Dao
interface FavoriteHolidayDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoliday(entity: FavouriteHolidayEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertType(entity: FavouritesTypesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounty(entity: FavouritesCountiesEntity)

    @Transaction
    suspend fun insert(holiday: FavouriteHolidayEntity,
                           types: List<FavouritesTypesEntity>,
                           counties: List<FavouritesCountiesEntity>) {
        insertHoliday(holiday)
        types.forEach { insertType(it) }
        counties.forEach { insertCounty(it) }
    }

    @Query("DELETE FROM favourite_holiday WHERE Id = :id")
    suspend fun deleteById(id: String)

    @Transaction
    @Query("SELECT * FROM favourite_holiday")
    fun observeAll(): Flow<List<FavoriteHolidayWithAll>>

}