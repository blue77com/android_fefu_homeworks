package com.example.android_fefu_homeworks.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import com.example.android_fefu_homeworks.data.HolidayRepository
import com.example.android_fefu_homeworks.model.Holiday
import com.example.android_fefu_homeworks.model.HolidayFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class HolidayViewModel @Inject constructor(
    private val repository: HolidayRepository
) : ViewModel() {

    var uiState by mutableStateOf(HolidayUiState())
        private set

    private var cachedHolidays by mutableStateOf(emptyList<Holiday>())
    private var searchJob: Job? = null
    private var holidaysLoadJob: Job? = null

    private var favouritesitems by mutableStateOf(emptyList<Holiday>())

    init {
        loadCountries()
        loadfavorites()
    }

    private fun loadfavorites() {
        viewModelScope.launch {
            try {
                val favs = repository.getFavourites()
                favouritesitems = favs
                uiState = uiState.copy(
                    favourites = favs.map { it.id }.toSet(),
                    favouritesError = null,
                )
                filterHolidays()
            } catch (ex: Exception) {
                uiState = uiState.copy(
                    favouritesError = ex.message ?: "Не удалось загрузить избранное",
                )
            }
        }
    }

    fun onQueryChange(query: String) {
        uiState = uiState.copy(query = query)
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

    fun dismissFavouriteActionError() {
        uiState = uiState.copy(favouriteActionError = null)
    }

    fun onToggleFavourite(holidayId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(favouriteActionError = null)
            val currentItems = favouritesitems
            val currentIds = uiState.favourites

            if (holidayId in currentIds) {
                try {
                    repository.removeFavorite(holidayId)
                    favouritesitems = currentItems.filterNot { it.id == holidayId }
                    uiState = uiState.copy(favourites = currentIds - holidayId)
                    if (uiState.filter == HolidayFilter.FAVOURITES) {
                        filterHolidays()
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    uiState = uiState.copy(
                        favouriteActionError = e.message ?: "Не удалось удалить из избранного",
                    )
                }
            } else {
                val holiday = findHolidayInMemory(holidayId)
                if (holiday == null) {
                    uiState = uiState.copy(
                        favouriteActionError =
                            "Праздник не найден в загруженном списке. Обновите список или выберите страну.",
                    )
                    return@launch
                }
                try {
                    repository.addFavorite(holiday)
                    favouritesitems = listOf(holiday) + currentItems
                    uiState = uiState.copy(favourites = currentIds + holidayId)
                    if (uiState.filter == HolidayFilter.FAVOURITES) {
                        filterHolidays()
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    uiState = uiState.copy(
                        favouriteActionError = e.message ?: "Не удалось добавить в избранное",
                    )
                }
            }
        }
    }

    fun retry() {
        if (uiState.favouritesError != null) {
            loadfavorites()
        }
        if (uiState.countriesError != null) {
            loadCountries()
        }
        if (uiState.listState is HolidayListState.Error && uiState.selectedCountryCode != null) {
            loadHolidays()
        }
    }

    fun refresh() {
        if (uiState.selectedCountryCode != null) {
            loadHolidays()
        }
    }

    private fun loadCountries() {
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoadingCountries = true,
                countriesError = null,
            )
            try {
                val countries = repository.getAvailableCountries()
                uiState = uiState.copy(
                    countries = countries,
                    isLoadingCountries = false,
                    countriesError = null,
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoadingCountries = false,
                    countriesError = e.message ?: "Ошибка загрузки стран",
                )
            }
        }
    }

    private fun loadHolidays() {
        val countryCode = uiState.selectedCountryCode ?: return
        val year = uiState.selectedYear

        holidaysLoadJob?.cancel()
        holidaysLoadJob = viewModelScope.launch {
            uiState = uiState.copy(listState = HolidayListState.Loading)
            try {
                val holidays = repository.getPublicHolidays(year, countryCode)
                ensureActive()
                cachedHolidays = holidays
                uiState = uiState.copy(listState = HolidayListState.Success(holidays))
                filterHolidays()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                uiState = uiState.copy(
                    listState = HolidayListState.Error(
                        "Ошибка загрузки праздников",
                    ),
                )
            }
        }
    }

    private fun filterHolidays() {
        if (uiState.listState is HolidayListState.Error) {
            return
        }

        val allHolidays = cachedHolidays

        if (allHolidays.isEmpty() && uiState.filter != HolidayFilter.FAVOURITES) {
            uiState = uiState.copy(listState = HolidayListState.Empty)
            return
        }

        val filtered = when {
            uiState.filter == HolidayFilter.FAVOURITES -> {
                if (uiState.query.isNotBlank()) {
                    val queryLower = uiState.query.lowercase()
                    favouritesitems.filter {
                        it.name.lowercase().contains(queryLower) ||
                                it.localName.lowercase().contains(queryLower)
                    }
                } else {
                    favouritesitems
                }
            }
            uiState.query.isNotBlank() -> {
                val queryLower = uiState.query.lowercase()
                cachedHolidays.filter {
                    it.name.lowercase().contains(queryLower) ||
                            it.localName.lowercase().contains(queryLower)
                }
            }
            else -> cachedHolidays
        }

        when {
            filtered.isEmpty() && (uiState.query.isNotBlank() || uiState.filter == HolidayFilter.FAVOURITES) -> {
                uiState = uiState.copy(listState = HolidayListState.Empty)
            }
            else -> {
                uiState = uiState.copy(listState = HolidayListState.Success(filtered))
            }
        }
    }

    private fun findHolidayInMemory(holidayId: String): Holiday? =
        cachedHolidays.firstOrNull { it.id == holidayId }
            ?: favouritesitems.firstOrNull { it.id == holidayId }
            ?: (uiState.listState as? HolidayListState.Success)
                ?.holidays
                ?.firstOrNull { it.id == holidayId }

    fun getHolidayById(holidayId: String): Holiday? = findHolidayInMemory(holidayId)
}
