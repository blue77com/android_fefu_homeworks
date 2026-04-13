package com.example.android_fefu_homeworks.ui.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.android_fefu_homeworks.model.Country
import com.example.android_fefu_homeworks.model.Holiday
import com.example.android_fefu_homeworks.ui.viewmodel.HolidayListState
import com.example.android_fefu_homeworks.ui.viewmodel.HolidayUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HolidayScreensUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun holidayListScreen_searchFiltersToRelevantItem() {
        val all = listOf(
            holiday(name = "New Year's Day", localName = "Новый год"),
            holiday(name = "Victory Day", localName = "День Победы"),
        )
        val state = HolidayUiState(
            listState = HolidayListState.Success(all),
            countries = listOf(Country("RU", "Russia")),
            selectedCountryCode = "RU",
        )
        val lastQuery = mutableStateOf("")

        composeTestRule.setContent {
            var currentState by mutableStateOf(state)
            MaterialTheme {
                HolidayListScreen(
                    state = currentState,
                    onQueryChange = { query ->
                        lastQuery.value = query
                        currentState = currentState.copy(
                            query = query,
                            listState = HolidayListState.Success(
                                all.filter {
                                    it.name.contains(query, ignoreCase = true) ||
                                        it.localName.contains(query, ignoreCase = true)
                                },
                            ),
                        )
                    },
                    onCountryChange = {},
                    onYearChange = {},
                    onFilterChange = {},
                    onToggleFavourite = {},
                    onHolidayClick = {},
                    onRetry = {},
                    onRefresh = {},
                    onDismissFavouriteActionError = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Поиск праздников").performTextInput("new")
        composeTestRule.waitForIdle()
        assertEquals("new", lastQuery.value)
        composeTestRule.onNodeWithText("New Year's Day").assertIsDisplayed()
        composeTestRule.onNodeWithText("Victory Day").assertDoesNotExist()
    }

    @Test
    fun holidayListScreen_clickFavoriteIcon_callsToggleWithHolidayId() {
        val item = holiday(name = "New Year's Day", localName = "Новый год")
        var toggledId: String? = null
        val state = HolidayUiState(
            countries = listOf(Country("RU", "Russia")),
            selectedCountryCode = "RU",
            listState = HolidayListState.Success(listOf(item)),
        )

        composeTestRule.setContent {
            MaterialTheme {
                HolidayListScreen(
                    state = state,
                    onQueryChange = {},
                    onCountryChange = {},
                    onYearChange = {},
                    onFilterChange = {},
                    onToggleFavourite = { toggledId = it },
                    onHolidayClick = {},
                    onRetry = {},
                    onRefresh = {},
                    onDismissFavouriteActionError = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add to favourites").performClick()
        assertEquals(item.id, toggledId)
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

