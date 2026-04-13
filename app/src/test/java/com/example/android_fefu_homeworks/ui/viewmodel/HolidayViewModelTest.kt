package com.example.android_fefu_homeworks.ui.viewmodel

import com.example.android_fefu_homeworks.model.Country
import com.example.android_fefu_homeworks.model.Holiday
import com.example.android_fefu_homeworks.MainDispatcherRule
import com.example.android_fefu_homeworks.FakeHolidayRepository
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent

class HolidayViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun onToggleFavourite_whenHolidayNotInMemory_setsActionError_andDoesNotCallRepository() = runTest {
        val repo = FakeHolidayRepository()
        val vm = HolidayViewModel(repository = repo)

        vm.onToggleFavourite("missing-id")
        advanceUntilIdle()

        assertNotNull(vm.uiState.favouriteActionError)
        assertEquals(0, repo.addFavoriteCalls)
        assertEquals(0, repo.removeFavoriteCalls)
    }

    @Test
    fun onQueryChange_debouncesFiltering_andUsesLatestQuery() = runTest {
        val repo = FakeHolidayRepository(
            favourites = emptyList(),
            countries = emptyList(),
            publicHolidaysProvider = { _, _ ->
                listOf(
                    holiday(name = "New Year", localName = "Новый год"),
                    holiday(name = "Victory Day", localName = "День Победы"),
                )
            },
        )
        val vm = HolidayViewModel(repository = repo)

        vm.onCountryChange("RU")
        advanceUntilIdle()

        vm.onQueryChange("new")
        advanceTimeBy(200)
        vm.onQueryChange("new y")
        advanceTimeBy(200)
        vm.onQueryChange("new ye")

        runCurrent()
        assertTrue(vm.uiState.query.isNotBlank())

        advanceTimeBy(401)
        advanceUntilIdle()

        val state = vm.uiState.listState as HolidayListState.Success
        assertEquals(1, state.holidays.size)
        assertEquals("New Year", state.holidays.first().name)
    }

    @Test
    fun retry_whenErrorsPresent_triggersReloads() = runTest {
        val repo = FakeHolidayRepository(
            favourites = emptyList(),
            countries = listOf(Country("RU", "Russia")),
            publicHolidaysProvider = { _, _ -> emptyList() },
        )
        repo.failFavourites = true
        repo.failCountries = true
        val vm = HolidayViewModel(repository = repo)
        advanceUntilIdle()

        repo.failPublicHolidays = true
        vm.onCountryChange("RU")
        advanceUntilIdle()

        assertNotNull(vm.uiState.countriesError)
        assertTrue(vm.uiState.listState is HolidayListState.Error)

        repo.failFavourites = false
        repo.failCountries = false
        repo.failPublicHolidays = false

        vm.retry()
        advanceUntilIdle()

        assertTrue(repo.getFavouritesCalls >= 2)
        assertTrue(repo.getAvailableCountriesCalls >= 2)
        assertTrue(repo.getPublicHolidaysCalls >= 2)
        assertNull(vm.uiState.countriesError)
        assertNull(vm.uiState.favouritesError)
    }

    private fun holiday(
        name: String,
        localName: String,
        date: String = "2026-01-01",
        countryCode: String = "RU",
    ): Holiday = Holiday(
        date = date,
        localName = localName,
        name = name,
        countryCode = countryCode,
        global = true,
        counties = null,
        types = listOf("Public"),
    )
}