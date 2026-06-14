package com.example.android_fefu_homeworks.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_fefu_homeworks.data.HolidayRepository
import com.example.android_fefu_homeworks.data.SettingsRepository
import com.example.android_fefu_homeworks.model.Holiday
import com.example.android_fefu_homeworks.model.Country
import com.example.android_fefu_homeworks.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject

sealed class ApiHolidaysState {
    data object IdleNoCountry : ApiHolidaysState()
    data object Loading : ApiHolidaysState()
    data class Error(val message: String) : ApiHolidaysState()
    data class Success(val holidays: List<Holiday>) : ApiHolidaysState()
}

@HiltViewModel
class HolidayViewModel @Inject constructor(
    private val repository: HolidayRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val detailHolidayCache = mutableMapOf<String, Holiday>()

    private val _country = MutableStateFlow<String?>(null)
    private val _year = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val _month = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _countriesState = MutableStateFlow(CountriesUi())
    private val _favouriteActionError = MutableStateFlow<String?>(null)
    private val _favouritesReload = MutableStateFlow(0)

    private val _refreshRequests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init {
        // Загружаем список стран и сохраненную страну
        loadCountries()
        viewModelScope.launch {
            settingsRepository.selectedCountryCode.first()?.let { savedCode ->
                _country.value = savedCode
            }
        }
    }

    fun onCountryChange(countryCode: String) {
        _country.value = countryCode
        viewModelScope.launch {
            settingsRepository.saveCountryCode(countryCode)
        }
    }

    fun onYearChange(year: Int) {
        val date = _selectedDate.value
        val newDate = try {
            date.withYear(year)
        } catch (e: Exception) {
            val lastDay = LocalDate.of(year, date.month, 1).lengthOfMonth()
            date.withYear(year).withDayOfMonth(lastDay)
        }
        onDateSelected(newDate)
    }

    fun onMonthChange(month: Int) {
        val date = _selectedDate.value
        val newDate = when {
            month < 0 -> date.minusMonths(1)
            month > 11 -> date.plusMonths(1)
            else -> {
                val targetMonth = month + 1
                val lastDay = LocalDate.of(date.year, targetMonth, 1).lengthOfMonth()
                date.withMonth(targetMonth).withDayOfMonth(minOf(date.dayOfMonth, lastDay))
            }
        }
        onDateSelected(newDate)
    }

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
        _month.value = date.monthValue - 1
        _year.value = date.year
    }

    fun goToToday() {
        val today = LocalDate.now()
        onDateSelected(today)
    }

    fun dismissFavouriteActionError() {
        _favouriteActionError.value = null
    }

    fun addNote(date: String, text: String) {
        viewModelScope.launch {
            repository.addNote(Note(date = date, text = text))
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    private val notesState: StateFlow<Map<String, List<Note>>> = repository.observeNotes()
        .map { notes -> notes.groupBy { it.date } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val favouritesOutcome: StateFlow<Result<List<Holiday>>> = _favouritesReload
        .flatMapLatest {
            repository.observeFavourites()
                .map { Result.success(it) }
                .catch { emit(Result.failure(it)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Result.success(emptyList()))

    private val reloadTrigger = merge(
        combine(_country, _year) { c, y -> Triple(c, y, false) }.distinctUntilChanged(),
        _refreshRequests.map { Triple(_country.value, _year.value, true) }
    )

    private val holidaysApiState: StateFlow<ApiHolidaysState> = reloadTrigger
        .flatMapLatest { (countryCode, year, isRefresh) ->
            flow<ApiHolidaysState> {
                if (countryCode == null) {
                    emit(ApiHolidaysState.IdleNoCountry)
                } else {
                    emit(ApiHolidaysState.Loading)
                    try {
                        val holidays = repository.getPublicHolidays(year, countryCode, forceRefresh = isRefresh)
                        emit(ApiHolidaysState.Success(holidays))
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        emit(ApiHolidaysState.Error("Ошибка загрузки данных"))
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ApiHolidaysState.IdleNoCountry
        )

    private val listState: StateFlow<HolidayListState> = holidaysApiState.map { api ->
        when (api) {
            is ApiHolidaysState.IdleNoCountry -> HolidayListState.Empty
            is ApiHolidaysState.Loading -> HolidayListState.Loading
            is ApiHolidaysState.Error -> HolidayListState.Error(api.message)
            is ApiHolidaysState.Success -> if (api.holidays.isEmpty()) HolidayListState.Empty else HolidayListState.Success(api.holidays)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HolidayListState.Empty)

    private val calendarPrefs = combine(_year, _month, _selectedDate, _favouriteActionError, notesState) { y, m, d, err, notes ->
        object {
            val year = y
            val month = m
            val selectedDate = d
            val favActionError = err
            val notes = notes
        }
    }

    val uiState: StateFlow<HolidayUiState> = combine(
        listState, 
        _countriesState, 
        _country, 
        calendarPrefs, 
        favouritesOutcome
    ) { listSt, countriesSt, country, prefs, favOutcome ->
        HolidayUiState(
            selectedCountryCode = country,
            selectedYear = prefs.year,
            selectedMonth = prefs.month,
            selectedDate = prefs.selectedDate,
            favourites = favOutcome.getOrElse { emptyList() }.map { it.id }.toSet(),
            notes = prefs.notes,
            countries = countriesSt.countries,
            listState = listSt,
            isLoadingCountries = countriesSt.loading,
            countriesError = countriesSt.error,
            favouritesError = favOutcome.exceptionOrNull()?.message,
            favouriteActionError = prefs.favActionError
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HolidayUiState())

    fun onToggleFavourite(holidayId: String) {
        viewModelScope.launch {
            val currentIds = favouritesOutcome.value.getOrElse { emptyList() }.map { it.id }.toSet()
            if (holidayId in currentIds) repository.removeFavorite(holidayId)
            else {
                val holiday = findHolidayInMemory(holidayId)
                if (holiday != null) repository.addFavorite(holiday)
            }
        }
    }

    fun retry() { 
        if (uiState.value.countriesError != null) loadCountries()
        else refresh()
    }
    
    fun refresh() { viewModelScope.launch { _refreshRequests.emit(Unit) } }

    private data class CountriesUi(
        val countries: List<Country> = emptyList(), 
        val loading: Boolean = false, 
        val error: String? = null
    )

    private fun loadCountries() {
        viewModelScope.launch {
            _countriesState.value = _countriesState.value.copy(loading = true)
            try {
                val countries = repository.getAvailableCountries()
                _countriesState.value = CountriesUi(countries = countries)
            } catch (e: Exception) {
                _countriesState.value = CountriesUi(error = "Ошибка загрузки стран")
            }
        }
    }

    fun getHolidayById(holidayId: String): Holiday? = findHolidayInMemory(holidayId)

    private fun findHolidayInMemory(holidayId: String): Holiday? {
        val listSt = listState.value
        return (listSt as? HolidayListState.Success)?.holidays?.firstOrNull { it.id == holidayId }
            ?: favouritesOutcome.value.getOrElse { emptyList() }.firstOrNull { it.id == holidayId }
    }
}
