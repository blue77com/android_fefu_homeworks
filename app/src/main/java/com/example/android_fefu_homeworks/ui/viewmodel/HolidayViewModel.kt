package com.example.android_fefu_homeworks.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_fefu_homeworks.data.HolidayRepository
import com.example.android_fefu_homeworks.data.SettingsRepository
import com.example.android_fefu_homeworks.model.*
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

    private val _country = settingsRepository.selectedCountryCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _year = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val _month = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _showOnlyNotes = MutableStateFlow(false)

    private val _refreshRequests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val countriesState: StateFlow<List<Country>> = repository.observeCountries()
        .onStart { refreshCountries() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private sealed class HolidaysRefreshState {
        data object InProgress : HolidaysRefreshState()
        data object Succeeded : HolidaysRefreshState()
        data object Failed : HolidaysRefreshState()
    }

    private val holidaysApiState: StateFlow<ApiHolidaysState> = combine(
        _country, _year, _refreshRequests.onStart { emit(Unit) }
    ) { countryCode, year, _ -> countryCode to year }
        .flatMapLatest { (countryCode, year) ->
            if (countryCode == null) {
                flowOf(ApiHolidaysState.IdleNoCountry)
            } else {
                val refreshState = MutableStateFlow<HolidaysRefreshState>(HolidaysRefreshState.InProgress)
                combine(
                    repository.observePublicHolidays(year, countryCode),
                    refreshState,
                ) { holidays, refresh ->
                    when {
                        holidays.isNotEmpty() -> ApiHolidaysState.Success(holidays)
                        refresh is HolidaysRefreshState.InProgress -> ApiHolidaysState.Loading
                        refresh is HolidaysRefreshState.Failed ->
                            ApiHolidaysState.Error("Ошибка загрузки данных")
                        else -> ApiHolidaysState.Success(emptyList())
                    }
                }.onStart {
                    launchRefresh(year, countryCode, refreshState)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApiHolidaysState.Loading)

    private val notesState: StateFlow<Map<String, List<Note>>> = repository.observeNotes()
        .map { notes -> notes.groupBy { it.date } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val uiState: StateFlow<HolidayUiState> = combine(
        combine(holidaysApiState, countriesState, _country) { api, list, code -> Triple(api, list, code) },
        combine(_year, _month, _selectedDate) { y, m, d -> Triple(y, m, d) },
        _showOnlyNotes,
        notesState
    ) { (apiState, countries, country), (year, month, selectedDate), showOnlyNotes, notes ->

        val listState = when (apiState) {
            is ApiHolidaysState.Success -> HolidayListState.Success(apiState.holidays)
            is ApiHolidaysState.Error -> HolidayListState.Error(apiState.message)
            is ApiHolidaysState.Loading -> HolidayListState.Loading
            else -> HolidayListState.Empty
        }

        val countryName = countries.find { it.countryCode == country }?.name

        HolidayUiState(
            selectedCountryCode = country,
            selectedCountryName = countryName,
            selectedYear = year,
            selectedMonth = month,
            selectedDate = selectedDate,
            showOnlyNotes = showOnlyNotes,
            notes = notes,
            countries = countries,
            listState = listState,
            isLoadingCountries = countries.isEmpty() && apiState is ApiHolidaysState.Loading
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HolidayUiState())

    fun goToToday() {
        val today = LocalDate.now()
        _selectedDate.value = today
        _year.value = today.year
        _month.value = today.monthValue - 1
    }

    fun onToggleShowOnlyNotes(show: Boolean) {
        _showOnlyNotes.value = show
    }

    fun onToggleNoteFavourite(noteId: String) {
        viewModelScope.launch {
            repository.getNoteById(noteId)?.let { note ->
                repository.updateNote(note.copy(isFavourite = !note.isFavourite))
            }
        }
    }

    fun toggleChecklistItem(noteId: String, itemIndex: Int) {
        viewModelScope.launch {
            repository.getNoteById(noteId)?.let { note ->
                val newList = note.checklist.toMutableList()
                if (itemIndex in newList.indices) {
                    val item = newList[itemIndex]
                    newList[itemIndex] = item.copy(isChecked = !item.isChecked)
                    repository.updateNote(note.copy(checklist = newList))
                }
            }
        }
    }

    fun retry() {
        refresh()
    }

    suspend fun getHolidayById(id: String): Holiday? {
        return (holidaysApiState.value as? ApiHolidaysState.Success)?.holidays?.find { it.id == id }
    }

    suspend fun getNoteById(id: String): Note? {
        return repository.getNoteById(id)
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

    private fun launchRefresh(
        year: Int,
        countryCode: String,
        refreshState: MutableStateFlow<HolidaysRefreshState>,
    ) {
        viewModelScope.launch {
            refreshState.value = HolidaysRefreshState.InProgress
            try {
                repository.refreshPublicHolidays(year, countryCode)
                refreshState.value = HolidaysRefreshState.Succeeded
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                refreshState.value = HolidaysRefreshState.Failed
            }
        }
    }

    private fun refreshCountries() {
        viewModelScope.launch {
            try {
                repository.refreshCountries()
            } catch (e: Exception) { }
        }
    }

    fun refresh() { _refreshRequests.tryEmit(Unit) }

    fun saveNote(note: Note) {
        viewModelScope.launch {
            if (repository.getNoteById(note.id) != null) {
                repository.updateNote(note)
            } else {
                repository.addNote(note)
            }
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch { repository.deleteNote(id) }
    }
}
