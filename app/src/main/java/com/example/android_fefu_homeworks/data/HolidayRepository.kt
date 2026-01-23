package com.example.android_fefu_homeworks.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.android_fefu_homeworks.NetworkModule
import com.example.android_fefu_homeworks.data.remote.NagerApi
import com.example.android_fefu_homeworks.data.remote.toDomain
import com.example.android_fefu_homeworks.model.Country
import com.example.android_fefu_homeworks.model.Holiday

class HolidayRepository(private val api: NagerApi = NetworkModule.api) {
    
    suspend fun getAvailableCountries(): List<Country> = withContext(Dispatchers.IO) {
        api.getAvailableCountries().map { it.toDomain() }
    }
    
    suspend fun getPublicHolidays(year: Int, countryCode: String): List<Holiday> = withContext(Dispatchers.IO) {
        api.getPublicHolidays(year, countryCode).map { it.toDomain() }
    }
}
