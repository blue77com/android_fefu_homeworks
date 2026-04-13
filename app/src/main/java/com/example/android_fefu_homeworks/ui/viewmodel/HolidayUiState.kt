package com.example.android_fefu_homeworks.ui.viewmodel

import com.example.android_fefu_homeworks.model.HolidayFilter

sealed class HolidayListState {
    data object Loading : HolidayListState()
    data class Error(val message: String) : HolidayListState()
    data object Empty : HolidayListState()
    data class Success(val holidays: List<com.example.android_fefu_homeworks.model.Holiday>) : HolidayListState()
}

data class HolidayUiState(
    val query: String = "",
    val selectedCountryCode: String? = null,
    val selectedYear: Int = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
    val filter: HolidayFilter = HolidayFilter.ALL,
    val favourites: Set<String> = emptySet(),
    val countries: List<com.example.android_fefu_homeworks.model.Country> = emptyList(),
    val listState: HolidayListState = HolidayListState.Empty,
    val isLoadingCountries: Boolean = false,
    val countriesError: String? = null,
    val favouritesError: String? = null,
    val favouriteActionError: String? = null,
)
