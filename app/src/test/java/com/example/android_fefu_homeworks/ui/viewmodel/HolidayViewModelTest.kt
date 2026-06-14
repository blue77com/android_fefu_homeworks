package com.example.android_fefu_homeworks.ui.viewmodel

import com.example.android_fefu_homeworks.model.Country
import com.example.android_fefu_homeworks.model.Holiday
import com.example.android_fefu_homeworks.MainDispatcherRule
import com.example.android_fefu_homeworks.FakeHolidayRepository
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HolidayViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun onToggleShowOnlyNotes_switchesNotesFilter() = runTest {
        val vm = HolidayViewModel(repository = FakeHolidayRepository())

        vm.onToggleShowOnlyNotes(true)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.showOnlyNotes)
    }

    @Test
    fun retry_whenErrorsPresent_triggersReloads() = runTest {
        val repo = FakeHolidayRepository(
            favourites = emptyList(),
            countries = listOf(Country("RU", "Russia")),
            publicHolidaysProvider = { _, _ -> emptyList() },
        )
        repo.failCountries = true
        val vm = HolidayViewModel(repository = repo)
        advanceUntilIdle()

        repo.failPublicHolidays = true
        vm.onCountryChange("RU")
        advanceUntilIdle()

        assertTrue(vm.uiState.value.countriesError != null)
        assertTrue(vm.uiState.value.listState is HolidayListState.Error)

        repo.failCountries = false
        repo.failPublicHolidays = false

        vm.retry()
        advanceUntilIdle()

        assertTrue(repo.getAvailableCountriesCalls >= 2)
        assertTrue(repo.getPublicHolidaysCalls >= 2)
        assertNull(vm.uiState.value.countriesError)
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
