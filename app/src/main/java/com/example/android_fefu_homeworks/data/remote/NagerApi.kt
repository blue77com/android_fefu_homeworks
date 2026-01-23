package com.example.android_fefu_homeworks.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface NagerApi {
    
    @GET("AvailableCountries")
    suspend fun getAvailableCountries(): List<CountryDto>
    
    @GET("PublicHolidays/{year}/{countryCode}")
    suspend fun getPublicHolidays(
        @Path("year") year: Int,
        @Path("countryCode") countryCode: String
    ): List<HolidayDto>
}
