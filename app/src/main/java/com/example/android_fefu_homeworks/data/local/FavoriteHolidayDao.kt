package com.example.android_fefu_homeworks.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction


@Dao
interface FavoriteHolidayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert (entity: FavouriteHolidayEntity)

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

    @Query(value = "DELETE FROM favourite_holiday WHERE id = :id")
    suspend fun deleteById(id: String)

    @Transaction
    @Query("SELECT * FROM favourite_holiday")
    suspend fun getAll(): List<FavoriteHolidayWithAll>


}