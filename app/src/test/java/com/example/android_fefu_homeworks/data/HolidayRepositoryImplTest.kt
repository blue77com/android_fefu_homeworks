package com.example.android_fefu_homeworks.data

import com.example.android_fefu_homeworks.data.local.CountryDao
import com.example.android_fefu_homeworks.data.local.HolidayCacheDao
import com.example.android_fefu_homeworks.data.local.NoteDao
import com.example.android_fefu_homeworks.data.remote.NagerApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class HolidayRepositoryImplTest {

    private val api: NagerApi = mockk(relaxed = true)
    private val noteDao: NoteDao = mockk(relaxed = true)
    private val holidayCacheDao: HolidayCacheDao = mockk(relaxed = true)
    private val countryDao: CountryDao = mockk(relaxed = true)

    private fun createRepo() = HolidayRepositoryImpl(
        api = api,
        noteDao = noteDao,
        holidayCacheDao = holidayCacheDao,
        countryDao = countryDao,
    )

    @Test
    fun refreshCountries_fetchesFromApiAndStoresInDao() = runTest {
        coEvery { api.getAvailableCountries() } returns listOf(
            com.example.android_fefu_homeworks.data.remote.CountryDto("RU", "Russia"),
            com.example.android_fefu_homeworks.data.remote.CountryDto("US", "United States"),
        )
        val repo = createRepo()

        repo.refreshCountries()

        coVerify(exactly = 1) { countryDao.deleteAll() }
        coVerify(exactly = 1) {
            countryDao.insertCountries(
                match { entities ->
                    entities.size == 2 &&
                        entities[0].countryCode == "RU" &&
                        entities[1].countryCode == "US"
                },
            )
        }
    }

    @Test
    fun refreshPublicHolidays_replacesCacheForCountryAndYear() = runTest {
        coEvery { api.getPublicHolidays(2026, "RU") } returns listOf(
            com.example.android_fefu_homeworks.data.remote.HolidayDto(
                date = "2026-01-01",
                localName = "Новый год",
                name = "New Year's Day",
                countryCode = "RU",
                global = true,
                counties = null,
                types = listOf("Public"),
            ),
        )
        val repo = createRepo()

        repo.refreshPublicHolidays(2026, "RU")

        coVerify(exactly = 1) { holidayCacheDao.deleteHolidays("RU", 2026) }
        coVerify(exactly = 1) {
            holidayCacheDao.insertHolidays(
                match { holidays ->
                    holidays.size == 1 && holidays.first().countryCode == "RU"
                },
            )
        }
    }
}
