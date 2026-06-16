package com.example.android_fefu_homeworks.ui.viewmodel

import com.example.android_fefu_homeworks.model.Country
import com.example.android_fefu_homeworks.model.Note
import java.time.LocalDate
import java.util.Calendar

sealed class HolidayListState {
    data object Loading : HolidayListState()
    data class Error(val message: String) : HolidayListState()
    data object Empty : HolidayListState()
    data class Success(val holidays: List<com.example.android_fefu_homeworks.model.Holiday>) : HolidayListState()
}

data class HolidayUiState(
    val selectedCountryCode: String? = null,
    val selectedCountryName: String? = null,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedDate: LocalDate = LocalDate.now(),
    val showOnlyNotes: Boolean = false,
    val notes: Map<String, List<Note>> = emptyMap(),
    val countries: List<Country> = emptyList(),
    val listState: HolidayListState = HolidayListState.Empty,
    val isLoadingCountries: Boolean = false,
)
