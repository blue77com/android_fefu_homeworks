package com.example.android_fefu_homeworks.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android_fefu_homeworks.model.Country

@Entity(tableName = "countries")
data class CountryEntity(
    @PrimaryKey val countryCode: String,
    val name: String
)

fun Country.toEntity() = CountryEntity(countryCode = countryCode, name = name)
fun CountryEntity.toDomain() = Country(countryCode = countryCode, name = name)
