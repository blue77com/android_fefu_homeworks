package com.example.android_fefu_homeworks

import com.example.android_fefu_homeworks.data.HolidayRepository
import com.example.android_fefu_homeworks.model.Country
import com.example.android_fefu_homeworks.model.Holiday
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeHolidayRepository(
    private val favourites: List<Holiday> = emptyList(),
    private val countries: List<Country> = emptyList(),
    private val publicHolidaysProvider: suspend (Int, String) -> List<Holiday> = { _, _ -> emptyList() },
) : HolidayRepository {

    var addFavoriteCalls: Int = 0
        private set
    var removeFavoriteCalls: Int = 0
        private set
    var getFavouritesCalls: Int = 0
        private set
    var observeFavouritesCalls: Int = 0
        private set
    var getAvailableCountriesCalls: Int = 0
        private set
    var getPublicHolidaysCalls: Int = 0
        private set

    var failFavourites: Boolean = false
    var failCountries: Boolean = false
    var failPublicHolidays: Boolean = false

    override suspend fun addFavorite(Holiday: Holiday) {
        addFavoriteCalls++
    }

    override suspend fun removeFavorite(id: String) {
        removeFavoriteCalls++
    }

    override suspend fun getFavourites(): List<Holiday> {
        getFavouritesCalls++
        if (failFavourites) {
            throw IOException("favourites failed")
        }
        return favourites
    }

    override fun observeFavourites(): Flow<List<Holiday>> = flow {
        observeFavouritesCalls++
        if (failFavourites) {
            throw IOException("favourites failed")
        }
        emit(favourites)
    }

    override suspend fun getAvailableCountries(): List<Country> {
        getAvailableCountriesCalls++
        if (failCountries) {
            throw IOException("countries failed")
        }
        return countries
    }

    override suspend fun getPublicHolidays(year: Int, countryCode: String): List<Holiday> {
        getPublicHolidaysCalls++
        if (failPublicHolidays) {
            throw IOException("holidays failed")
        }
        return publicHolidaysProvider(year, countryCode)
    }
}
