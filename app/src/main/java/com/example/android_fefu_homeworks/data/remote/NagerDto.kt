package com.example.android_fefu_homeworks.data.remote

import com.example.android_fefu_homeworks.model.Country
import com.example.android_fefu_homeworks.model.Holiday

data class CountryDto(
    val countryCode: String,
    val name: String
)

fun CountryDto.toDomain(): Country = Country(
    countryCode = countryCode,
    name = name
)

data class HolidayDto(
    val date: String,
    val localName: String,
    val name: String,
    val countryCode: String,
    val global: Boolean,
    val counties: List<String>?,
    val launchYear: Int?,
    val types: List<String>
)

fun HolidayDto.toDomain(): Holiday = Holiday(
    date = date,
    localName = localName,
    name = name,
    countryCode = countryCode,
    global = global,
    counties = counties,
    launchYear = launchYear,
    types = types
)
