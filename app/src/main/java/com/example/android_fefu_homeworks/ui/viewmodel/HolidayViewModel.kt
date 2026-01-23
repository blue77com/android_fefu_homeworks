package com.example.android_fefu_homeworks.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.android_fefu_homeworks.data.HolidayRepository
import com.example.android_fefu_homeworks.model.Holiday
import com.example.android_fefu_homeworks.model.HolidayFilter

class HolidayViewModel(
    private val repository: HolidayRepository = HolidayRepository()
) : ViewModel() {

    var uiState by mutableStateOf(HolidayUiState())
        private set

    private var cachedHolidays: List<Holiday> = emptyList()
    private var searchJob: Job? = null

    init {
        loadCountries()
    }

    fun onQueryChange(query: String) {
        uiState = uiState.copy(query = query)
        // Debounce поиска
        searchJob?.cancel()
        if (query.isNotBlank()) {
            searchJob = viewModelScope.launch {
                delay(400)
                filterHolidays()
            }
        } else {
            filterHolidays()
        }
    }

    fun onCountryChange(countryCode: String) {
        uiState = uiState.copy(selectedCountryCode = countryCode, query = "")
        loadHolidays()
    }

    fun onYearChange(year: Int) {
        uiState = uiState.copy(selectedYear = year)
        if (uiState.selectedCountryCode != null) {
            loadHolidays()
        }
    }

    fun onFilterChange(filter: HolidayFilter) {
        uiState = uiState.copy(filter = filter)
        filterHolidays()
    }

    fun onToggleFavourite(holidayId: String) {
        val favourites = uiState.favourites
        uiState = uiState.copy(
            favourites = if (holidayId in favourites) {
                favourites - holidayId
            } else {
                favourites + holidayId
            }
        )
        filterHolidays()
    }

    fun retry() {
        if (uiState.selectedCountryCode != null) {
            loadHolidays()
        } else {
            loadCountries()
        }
    }

    fun refresh() {
        if (uiState.selectedCountryCode != null) {
            loadHolidays()
        }
    }

    private fun loadCountries() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoadingCountries = true)
            try {
                val countries = repository.getAvailableCountries()
                uiState = uiState.copy(
                    countries = countries,
                    isLoadingCountries = false
                )
            } catch (e: Exception) {
                uiState = uiState.copy(isLoadingCountries = false)
            }
        }
    }

    private fun loadHolidays() {
        val countryCode = uiState.selectedCountryCode ?: return
        val year = uiState.selectedYear

        viewModelScope.launch {
            uiState = uiState.copy(listState = HolidayListState.Loading)
            try {
                val holidays = repository.getPublicHolidays(year, countryCode)
                cachedHolidays = holidays
                uiState = uiState.copy(listState = HolidayListState.Success(holidays))
                filterHolidays()
            } catch (e: Exception) {
                uiState = uiState.copy(
                    listState = HolidayListState.Error(
                        e.message ?: "Ошибка загрузки праздников"
                    )
                )
            }
        }
    }

    private fun filterHolidays() {
        // Всегда используем cachedHolidays как источник всех праздников
        // Это гарантирует, что при переключении фильтра мы работаем с полным списком
        val allHolidays = cachedHolidays

        if (allHolidays.isEmpty()) {
            // Если праздники еще не загружены, не меняем состояние
            // (оно может быть Loading или Error)
            return
        }

        val filtered = when {
            uiState.filter == HolidayFilter.FAVOURITES -> {
                allHolidays.filter { it.id in uiState.favourites }
            }
            uiState.query.isNotBlank() -> {
                val queryLower = uiState.query.lowercase()
                allHolidays.filter {
                    it.name.lowercase().contains(queryLower) ||
                    it.localName.lowercase().contains(queryLower)
                }
            }
            else -> allHolidays
        }

        when {
            filtered.isEmpty() && (uiState.query.isNotBlank() || uiState.filter == HolidayFilter.FAVOURITES) -> {
                uiState = uiState.copy(listState = HolidayListState.Empty)
            }
            filtered.isEmpty() -> {
                uiState = uiState.copy(listState = HolidayListState.Empty)
            }
            else -> {
                uiState = uiState.copy(listState = HolidayListState.Success(filtered))
            }
        }
    }

    fun getHolidayById(holidayId: String): Holiday? {
        return when (val state = uiState.listState) {
            is HolidayListState.Success -> state.holidays.find { it.id == holidayId }
            else -> cachedHolidays.find { it.id == holidayId }
        }
    }
}
