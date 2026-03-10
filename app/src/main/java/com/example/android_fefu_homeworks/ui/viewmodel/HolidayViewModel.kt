package com.example.android_fefu_homeworks.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.query
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.android_fefu_homeworks.data.HolidayRepository
import com.example.android_fefu_homeworks.data.HolidayRepositoryImpl
import com.example.android_fefu_homeworks.model.Holiday
import com.example.android_fefu_homeworks.model.HolidayFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class HolidayViewModel @Inject constructor(
    private val repository: HolidayRepositoryImpl
) : ViewModel() {

    var uiState by mutableStateOf(HolidayUiState())
        private set

    private var cachedHolidays by mutableStateOf(emptyList<Holiday>())
    private var searchJob: Job? = null

    private var favouritesitems by mutableStateOf(emptyList<Holiday>())

    init {
        loadCountries()
        loadfavorites()
    }

    private fun loadfavorites(){
        viewModelScope.launch {
            try {
                val favs = repository.getFavourites()
                favouritesitems = favs
                uiState = uiState.copy(favourites = favs.map{it.id}.toSet())
            }   catch (ex: Exception) {
                uiState = uiState.copy(
                    listState = HolidayListState.Error(
                        ex.message ?: "Не удалось загрузить избранное"
                    )
                )
            }
        }
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
        viewModelScope.launch {
            val currentItems = favouritesitems
            val currentIds = uiState.favourites

            if (holidayId in currentIds) {
                repository.removeFavorite(holidayId)
                favouritesitems = currentItems.filterNot { it.id ==holidayId }
                uiState = uiState.copy(favourites = currentIds - holidayId)
            }
            else {
                val holiday = cachedHolidays.firstOrNull() { it.id == holidayId }
                if (holiday == null){
                    uiState = uiState.copy(listState = HolidayListState.Error(
                         "Не удалось загрузить избранное"
                    ))
                    return@launch
                }
                repository.addFavorite(holiday)
                favouritesitems = listOf(holiday) + currentItems
                uiState = uiState.copy(favourites = currentIds + holidayId)
            }
        }
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

        if (allHolidays.isEmpty() && uiState.filter != HolidayFilter.FAVOURITES) {
            // Если праздники еще не загружены, не меняем состояние
            // (оно может быть Loading или Error)
            return
        }

        val filtered = when {
            uiState.filter == HolidayFilter.FAVOURITES -> favouritesitems
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
