package com.example.android_fefu_homeworks.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.android_fefu_homeworks.NetworkModule
import com.example.android_fefu_homeworks.data.local.FavoriteHolidayDao
import com.example.android_fefu_homeworks.data.local.FavouriteHolidayEntity
import com.example.android_fefu_homeworks.data.local.FavouritesCountiesEntity
import com.example.android_fefu_homeworks.data.local.FavouritesTypesEntity
import com.example.android_fefu_homeworks.data.local.toDomain
import com.example.android_fefu_homeworks.data.remote.CountryDto
import com.example.android_fefu_homeworks.data.remote.NagerApi
import com.example.android_fefu_homeworks.data.remote.toDomain
import com.example.android_fefu_homeworks.model.Country
import com.example.android_fefu_homeworks.model.Holiday
import retrofit2.http.Path
import javax.inject.Inject
import javax.inject.Singleton


interface HolidayRepository {
    suspend fun addFavorite(Holiday: Holiday)

    suspend fun removeFavorite(id: String)

    suspend fun getFavourites(): List<Holiday>

    suspend fun getAvailableCountries(): List<Country>
    suspend fun getPublicHolidays(year: Int, countryCode: String) : List<Holiday>
}

@Singleton
class HolidayRepositoryImpl @Inject constructor(
    private val api: NagerApi,
    private val favoriteHolidayDao: FavoriteHolidayDao,
) : HolidayRepository{

    override suspend fun addFavorite(Holiday: Holiday) = withContext(Dispatchers.IO)
    {
        val holidayEntity = FavouriteHolidayEntity(
            id =  Holiday.id,
            date = Holiday.date,
            localName = Holiday.localName ,
            name = Holiday.name,
            countryCode = Holiday.countryCode,
            global = Holiday.global
        )

        val typeEntities = Holiday.types?.map { typeName ->
            FavouritesTypesEntity(
                holidayId = Holiday.id,
                name = typeName
            )
        } ?: emptyList()

        val countyEntities = Holiday.counties?.map { countyCode ->
            FavouritesCountiesEntity(
                holidayId = Holiday.id,
                name = countyCode
            )
        } ?: emptyList()

        favoriteHolidayDao.insert(holidayEntity, typeEntities, countyEntities)
    }

    override suspend fun getFavourites(): List<Holiday> = withContext(Dispatchers.IO){
        favoriteHolidayDao.getAll().map{ it.toDomain() }
    }

    override suspend fun removeFavorite(id: String) = withContext(Dispatchers.IO){
        favoriteHolidayDao.deleteById(id)
    }

    override suspend fun getAvailableCountries(): List<Country> = withContext(Dispatchers.IO) {
        api.getAvailableCountries().map { it.toDomain() }
    }
    
    override suspend fun getPublicHolidays(year: Int, countryCode: String): List<Holiday> = withContext(Dispatchers.IO) {
        api.getPublicHolidays(year, countryCode).map { it.toDomain() }
    }
}
