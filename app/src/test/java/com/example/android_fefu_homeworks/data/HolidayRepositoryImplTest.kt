package com.example.android_fefu_homeworks.data

import com.example.android_fefu_homeworks.data.local.FavoriteHolidayDao
import com.example.android_fefu_homeworks.data.local.FavouritesCountiesEntity
import com.example.android_fefu_homeworks.data.local.FavouritesTypesEntity
import com.example.android_fefu_homeworks.data.local.FavouriteHolidayEntity
import com.example.android_fefu_homeworks.data.remote.NagerApi
import com.example.android_fefu_homeworks.model.Holiday
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class HolidayRepositoryImplTest {

    private val api: NagerApi = mockk(relaxed = true)
    private val dao: FavoriteHolidayDao = mockk(relaxed = true)

    @Test
    fun addFavorite_mapsDomainToEntities_andCallsDaoInsert() = runTest {
        val repo = HolidayRepositoryImpl(api = api, favoriteHolidayDao = dao)
        val holiday = Holiday(
            date = "2026-01-01",
            localName = "Новый год",
            name = "New Year's Day",
            countryCode = "RU",
            global = true,
            counties = listOf("RU-PRI"),
            types = listOf("Public", "Bank"),
        )

        repo.addFavorite(holiday)

        val expectedHolidayEntity = FavouriteHolidayEntity(
            id = holiday.id,
            date = holiday.date,
            localName = holiday.localName,
            name = holiday.name,
            countryCode = holiday.countryCode,
            global = holiday.global,
        )
        val expectedTypes = listOf(
            FavouritesTypesEntity(holidayId = holiday.id, name = "Public"),
            FavouritesTypesEntity(holidayId = holiday.id, name = "Bank"),
        )
        val expectedCounties = listOf(
            FavouritesCountiesEntity(holidayId = holiday.id, name = "RU-PRI"),
        )

        coVerify(exactly = 1) {
            dao.insert(
                expectedHolidayEntity,
                match { it.map { e -> e.copy(id = 0) } == expectedTypes.map { e -> e.copy(id = 0) } },
                match { it.map { e -> e.copy(id = 0) } == expectedCounties.map { e -> e.copy(id = 0) } },
            )
        }
    }

    @Test
    fun addFavorite_whenCountiesNull_insertsEmptyCountiesList() = runTest {
        val repo = HolidayRepositoryImpl(api = api, favoriteHolidayDao = dao)
        val holiday = Holiday(
            date = "2026-02-01",
            localName = "x",
            name = "x",
            countryCode = "RU",
            global = true,
            counties = null,
            types = listOf("Public"),
        )

        repo.addFavorite(holiday)

        coVerify(exactly = 1) {
            dao.insert(
                any<FavouriteHolidayEntity>(),
                any<List<FavouritesTypesEntity>>(),
                match { it.isEmpty() },
            )
        }
    }

    @Test
    fun getAvailableCountries_mapsApiDtosToDomain() = runTest {
        coEvery { api.getAvailableCountries() } returns listOf(
            com.example.android_fefu_homeworks.data.remote.CountryDto("RU", "Russia"),
            com.example.android_fefu_homeworks.data.remote.CountryDto("US", "United States"),
        )
        val repo = HolidayRepositoryImpl(api = api, favoriteHolidayDao = dao)

        val result = repo.getAvailableCountries()

        assertEquals(2, result.size)
        assertEquals("RU", result[0].countryCode)
        assertEquals("Russia", result[0].name)
        assertEquals("US", result[1].countryCode)
        assertEquals("United States", result[1].name)
    }
}

