package com.example.android_fefu_homeworks.ui.viewmodel

import com.example.android_fefu_homeworks.model.Holiday
import com.example.android_fefu_homeworks.model.HolidayFilter

internal sealed interface ApiHolidaysState {
    data object IdleNoCountry : ApiHolidaysState
    data object Loading : ApiHolidaysState
    data class Success(val holidays: List<Holiday>) : ApiHolidaysState
    data class Error(val message: String) : ApiHolidaysState
}

internal object HolidayListPresentationBuilder {
    fun build(
        debouncedQuery: String,
        filter: HolidayFilter,
        favResult: Result<List<Holiday>>,
        api: ApiHolidaysState,
    ): HolidayListState {
        val favouritesList = favResult.getOrElse { emptyList() }

        if (filter == HolidayFilter.FAVOURITES) {
            val filtered = applySearch(favouritesList, debouncedQuery)
            return if (filtered.isEmpty()) HolidayListState.Empty else HolidayListState.Success(filtered)
        }

        if (api is ApiHolidaysState.Error) {
            return HolidayListState.Error(api.message)
        }
        if (api is ApiHolidaysState.Loading) {
            return HolidayListState.Loading
        }

        val cachedFromApi = when (api) {
            is ApiHolidaysState.Success -> api.holidays
            is ApiHolidaysState.IdleNoCountry -> emptyList()
            is ApiHolidaysState.Error -> emptyList()
            is ApiHolidaysState.Loading -> emptyList()
        }

        if (api is ApiHolidaysState.IdleNoCountry) {
            return HolidayListState.Empty
        }

        if (cachedFromApi.isEmpty()) {
            return HolidayListState.Empty
        }

        val filtered = applySearch(cachedFromApi, debouncedQuery)
        return if (filtered.isEmpty()) HolidayListState.Empty else HolidayListState.Success(filtered)
    }

    private fun applySearch(holidays: List<Holiday>, query: String): List<Holiday> {
        if (query.isBlank()) return holidays
        val q = query.lowercase()
        return holidays.filter {
            it.name.lowercase().contains(q) ||
                it.localName.lowercase().contains(q)
        }
    }
}
