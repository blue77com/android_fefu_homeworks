package com.example.android_fefu_homeworks.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_fefu_homeworks.data.HolidayRepository
import com.example.android_fefu_homeworks.data.SettingsRepository
import com.example.android_fefu_homeworks.model.Holiday
import com.example.android_fefu_homeworks.model.Country
import com.example.android_fefu_homeworks.model.Note
import com.example.android_fefu_homeworks.model.RepeatMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.abs

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

    private val _country = settingsRepository.selectedCountryCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        
    private val _year = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val _month = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _showOnlyNotes = MutableStateFlow(false)
    private val _countriesState = MutableStateFlow(CountriesUi())
    private val _refreshRequests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init {
        loadCountries()
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

    fun onToggleShowOnlyNotes(show: Boolean) {
        _showOnlyNotes.value = show
    }

    fun goToToday() {
        val today = LocalDate.now()
        onDateSelected(today)
    }

    fun onToggleNoteFavourite(id: String) {
        viewModelScope.launch {
            repository.toggleNoteFavourite(id)
        }
    }

    fun saveNote(note: Note) {
        viewModelScope.launch {
            if (repository.getNoteById(note.id) != null) {
                repository.updateNote(note)
            } else {
                repository.addNote(note)
            }
        }
    }

    fun toggleChecklistItem(noteId: String, itemId: String) {
        viewModelScope.launch {
            val note = repository.getNoteById(noteId) ?: return@launch
            val updatedChecklist = note.checklist.map {
                if (it.id == itemId) it.copy(isChecked = !it.isChecked) else it
            }
            repository.updateNote(note.copy(checklist = updatedChecklist))
        }
    }

    suspend fun getNoteById(id: String): Note? {
        return repository.getNoteById(id)
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    private val notesState: StateFlow<Map<String, List<Note>>> = repository.observeNotes()
        .map { notes -> notes.groupBy { it.date } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

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

    private val calendarPrefs: Flow<CalendarPrefs> = combine(
        _year,
        _month,
        _selectedDate,
        notesState
    ) { y, m, d, notes ->
        CalendarPrefs(y, m, d, notes)
    }

    val uiState: StateFlow<HolidayUiState> = combine(
        listState,
        _countriesState,
        _country,
        calendarPrefs,
        _showOnlyNotes
    ) { listSt, countriesSt, country, prefs, showOnlyNotes ->
        val displayedListState = if (showOnlyNotes) {
            HolidayListState.Empty 
        } else {
            listSt
        }

        val filteredNotes = if (showOnlyNotes) {
            prefs.notes.mapValues { entry -> 
                entry.value.filter { it.isFavourite } 
            }.filterValues { it.isNotEmpty() }
        } else {
            prefs.notes
        }

        val countryName = countriesSt.countries.find { it.countryCode == country }?.name

        HolidayUiState(
            selectedCountryCode = country,
            selectedCountryName = countryName,
            selectedYear = prefs.year,
            selectedMonth = prefs.month,
            selectedDate = prefs.selectedDate,
            showOnlyNotes = showOnlyNotes,
            notes = filteredNotes,
            countries = countriesSt.countries,
            listState = displayedListState,
            isLoadingCountries = countriesSt.loading,
            countriesError = countriesSt.error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HolidayUiState()
    )

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
    }
}

private data class CalendarPrefs(
    val year: Int,
    val month: Int,
    val selectedDate: LocalDate,
    val notes: Map<String, List<Note>>
)
