package com.example.android_fefu_homeworks.ui.viewmodel

import com.example.android_fefu_homeworks.FakeHolidayRepository
import com.example.android_fefu_homeworks.MainDispatcherRule
import com.example.android_fefu_homeworks.fakeSettingsRepository
import com.example.android_fefu_homeworks.model.Country
import com.example.android_fefu_homeworks.model.Holiday
import com.example.android_fefu_homeworks.model.ChecklistItem
import com.example.android_fefu_homeworks.model.Note
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class HolidayViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun onToggleShowOnlyNotes_switchesNotesFilter() = runTest {
        val vm = HolidayViewModel(
            repository = FakeHolidayRepository(),
            settingsRepository = fakeSettingsRepository(),
        )

        vm.onToggleShowOnlyNotes(true)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.showOnlyNotes)
    }

    @Test
    fun retry_afterRefreshFailure_triggersReload() = runTest {
        val repo = FakeHolidayRepository(
            initialCountries = listOf(Country("RU", "Russia")),
        )
        repo.failPublicHolidays = true

        val vm = HolidayViewModel(
            repository = repo,
            settingsRepository = fakeSettingsRepository("RU"),
        )
        advanceUntilIdle()

        assertTrue(vm.uiState.value.listState is HolidayListState.Error)

        repo.failPublicHolidays = false
        repo.publicHolidaysProvider = { _, _ -> listOf(testHoliday()) }
        vm.retry()
        advanceUntilIdle()

        assertTrue(repo.refreshPublicHolidaysCalls >= 2)
        assertTrue(vm.uiState.value.listState is HolidayListState.Success)
    }

    @Test
    fun onMonthChange_updatesSelectedDate() = runTest {
        val vm = HolidayViewModel(
            repository = FakeHolidayRepository(),
            settingsRepository = fakeSettingsRepository(),
        )
        advanceUntilIdle()

        val initialMonth = vm.uiState.value.selectedDate.monthValue
        vm.onMonthChange(vm.uiState.value.selectedMonth + 1)
        advanceUntilIdle()

        val expectedMonth = if (initialMonth == 12) 1 else initialMonth + 1
        assertEquals(expectedMonth, vm.uiState.value.selectedDate.monthValue)
    }

    @Test
    fun whenCountryNull_listStateIsEmpty() = runTest {
        val vm = HolidayViewModel(
            repository = FakeHolidayRepository(),
            settingsRepository = fakeSettingsRepository(initialCountryCode = null),
        )
        advanceUntilIdle()

        assertEquals(null, vm.uiState.value.selectedCountryCode)
        assertTrue(vm.uiState.value.listState is HolidayListState.Empty)
    }

    @Test
    fun toggleChecklistItem_togglesItemAtIndex() = runTest {
        val note = Note(
            id = "note-1",
            date = "2026-06-15",
            text = "Список дел",
            checklist = listOf(
                ChecklistItem(id = "c1", text = "Пункт 1", isChecked = false),
                ChecklistItem(id = "c2", text = "Пункт 2", isChecked = false),
            ),
        )
        val repo = FakeHolidayRepository(initialNotes = listOf(note))
        val vm = HolidayViewModel(
            repository = repo,
            settingsRepository = fakeSettingsRepository(),
        )
        advanceUntilIdle()

        vm.toggleChecklistItem("note-1", itemIndex = 0)
        advanceUntilIdle()

        val updated = repo.getNoteById("note-1")
        assertTrue(updated!!.checklist[0].isChecked)
        assertTrue(!updated.checklist[1].isChecked)
    }

    @Test
    fun saveNote_whenNew_insertsViaRepository() = runTest {
        val repo = FakeHolidayRepository()
        val vm = HolidayViewModel(
            repository = repo,
            settingsRepository = fakeSettingsRepository(),
        )
        advanceUntilIdle()

        val note = Note(id = "new-note", date = "2026-07-01", text = "Новая заметка")
        vm.saveNote(note)
        advanceUntilIdle()

        assertEquals("Новая заметка", repo.getNoteById("new-note")?.text)
    }

    private fun testHoliday(
        name: String = "New Year's Day",
        localName: String = "Новый год",
        date: String = LocalDate.now().toString(),
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
