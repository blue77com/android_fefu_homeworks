package com.example.android_fefu_homeworks.ui.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.example.android_fefu_homeworks.model.Country
import com.example.android_fefu_homeworks.model.Holiday
import com.example.android_fefu_homeworks.ui.viewmodel.HolidayListState
import com.example.android_fefu_homeworks.ui.viewmodel.HolidayUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HolidayScreensUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun holidayListScreen_notesToggle_showsOnlyNotesMode() {
        var showOnlyNotes = false
        val state = HolidayUiState(
            countries = listOf(Country("RU", "Russia")),
            selectedCountryCode = "RU",
            listState = HolidayListState.Success(emptyList()),
        )

        composeTestRule.setContent {
            MaterialTheme {
                HolidayListScreen(
                    state = state.copy(showOnlyNotes = showOnlyNotes),
                    onCountryChange = {},
                    onYearChange = {},
                    onMonthChange = {},
                    onDateSelected = {},
                    onGoToToday = {},
                    onToggleShowOnlyNotes = { showOnlyNotes = it },
                    onHolidayClick = {},
                    onAddNoteClick = {},
                    onNoteClick = {},
                    onToggleNoteFavourite = {},
                    onDeleteNote = {},
                    onRetry = {},
                    onRefresh = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Только избранные заметки").performClick()
        assertTrue(showOnlyNotes)
    }

    @Test
    fun holidayDetailScreen_showsHolidayName() {
        val holiday = holiday(name = "New Year's Day", localName = "Новый год")

        composeTestRule.setContent {
            MaterialTheme {
                HolidayDetailScreen(
                    holiday = holiday,
                    onBackClick = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Назад").assertIsDisplayed()
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
