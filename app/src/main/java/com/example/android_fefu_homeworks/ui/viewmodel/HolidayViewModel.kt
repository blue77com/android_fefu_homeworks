package com.example.android_fefu_homeworks.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_fefu_homeworks.data.HolidayRepository
import com.example.android_fefu_homeworks.model.Holiday
import com.example.android_fefu_homeworks.model.HolidayFilter
import com.example.android_fefu_homeworks.model.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HolidayViewModel @Inject constructor(
    private val repository: HolidayRepository,
) : ViewModel() {
    private val detailHolidayCache = mutableMapOf<String, Holiday>()

    private val _query = MutableStateFlow("")
    private val _filter = MutableStateFlow(HolidayFilter.ALL)
    private val _country = MutableStateFlow<String?>(null)
    private val _year = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val _countriesState = MutableStateFlow(CountriesUi())
    private val _favouriteActionError = MutableStateFlow<String?>(null)
    private val _favouritesReload = MutableStateFlow(0)

    private val _refreshRequests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init {
        loadCountries()
    }

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun onCountryChange(countryCode: String) {
        _country.value = countryCode
        _query.value = ""
    }

    fun onYearChange(year: Int) {
        _year.value = year
    }

    fun onFilterChange(filter: HolidayFilter) {
        _filter.value = filter
    }

    fun dismissFavouriteActionError() {
        _favouriteActionError.value = null
    }

    private val debouncedQuery = _query
        .debounce { q -> if (q.isBlank()) 0L else 400L }
        .distinctUntilChanged()

    private val favouritesOutcome: StateFlow<Result<List<Holiday>>> = _favouritesReload
        .flatMapLatest {
            repository.observeFavourites()
                .map { Result.success(it) }
                .catch { emit(Result.failure(it)) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Result.success(emptyList()),
        )

    private val reloadKeys: Flow<Pair<String?, Int>> = merge(
        combine(_country, _year) { c, y -> Pair(c, y) }.distinctUntilChanged(),
        _refreshRequests.map { Pair(_country.value, _year.value) },
    )

    private val holidaysApiState: StateFlow<ApiHolidaysState> = reloadKeys
        .flatMapLatest { (countryCode, year) ->
            if (countryCode == null) {
                flow { emit(ApiHolidaysState.IdleNoCountry) }
            } else {
                flow {
                    emit(ApiHolidaysState.Loading)
                    try {
                        val holidays = repository.getPublicHolidays(year, countryCode)
                        emit(ApiHolidaysState.Success(holidays))
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        emit(ApiHolidaysState.Error("Ошибка загрузки праздников"))
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ApiHolidaysState.IdleNoCountry,
        )

    private val listPresentation: StateFlow<HolidayListState> = combine(
        debouncedQuery,
        _filter,
        favouritesOutcome,
        holidaysApiState,
    ) { debounced, filter, favResult, api ->
        HolidayListPresentationBuilder.build(debounced, filter, favResult, api)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HolidayListState.Empty,
    )

    private data class CoreUi(
        val query: String,
        val listState: HolidayListState,
        val favResult: Result<List<Holiday>>,
        val filter: HolidayFilter,
    )

    private val coreUi: StateFlow<CoreUi> = combine(
        _query,
        listPresentation,
        favouritesOutcome,
        _filter,
    ) { query, listState, favResult, filter ->
        CoreUi(query, listState, favResult, filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CoreUi("", HolidayListState.Empty, Result.success(emptyList()), HolidayFilter.ALL),
    )

    val uiState: StateFlow<HolidayUiState> = combine(
        coreUi,
        _countriesState,
        _country,
        _year,
        _favouriteActionError,
    ) { core, countriesSt, country, year, favActionErr ->
        HolidayUiState(
            query = core.query,
            selectedCountryCode = country,
            selectedYear = year,
            filter = core.filter,
            favourites = core.favResult.getOrElse { emptyList() }.map { it.id }.toSet(),
            countries = countriesSt.countries,
            listState = core.listState,
            isLoadingCountries = countriesSt.loading,
            countriesError = countriesSt.error,
            favouritesError = core.favResult.exceptionOrNull()?.let { e ->
                e.message ?: "Не удалось загрузить избранное"
            },
            favouriteActionError = favActionErr,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HolidayUiState(),
    )

    fun onToggleFavourite(holidayId: String) {
        viewModelScope.launch {
            _favouriteActionError.value = null
            val currentIds =
                favouritesOutcome.value.getOrElse { emptyList() }.map { it.id }.toSet()

            if (holidayId in currentIds) {
                try {
                    repository.removeFavorite(holidayId)
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    _favouriteActionError.value =
                        e.message ?: "Не удалось удалить из избранного"
                }
            } else {
                val holiday = findHolidayInMemory(holidayId) ?: detailHolidayCache[holidayId]
                if (holiday == null) {
                    _favouriteActionError.value =
                        "Праздник не найден в загруженном списке. Обновите список или выберите страну."
                    return@launch
                }
                try {
                    repository.addFavorite(holiday)
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    _favouriteActionError.value =
                        e.message ?: "Не удалось добавить в избранное"
                }
            }
        }
    }

    fun retry() {
        if (uiState.value.favouritesError != null) {
            _favouritesReload.value += 1
        }
        if (uiState.value.countriesError != null) {
            loadCountries()
        }
        if (uiState.value.listState is HolidayListState.Error && uiState.value.selectedCountryCode != null) {
            viewModelScope.launch { _refreshRequests.emit(Unit) }
        }
    }

    fun refresh() {
        if (_country.value != null) {
            viewModelScope.launch { _refreshRequests.emit(Unit) }
        }
    }

    private data class CountriesUi(
        val countries: List<Country> = emptyList(),
        val loading: Boolean = false,
        val error: String? = null,
    )

    private fun loadCountries() {
        viewModelScope.launch {
            _countriesState.value = _countriesState.value.copy(
                loading = true,
                error = null,
            )
            try {
                val countries = repository.getAvailableCountries()
                _countriesState.value = CountriesUi(
                    countries = countries,
                    loading = false,
                    error = null,
                )
            } catch (e: Exception) {
                _countriesState.value = CountriesUi(
                    countries = emptyList(),
                    loading = false,
                    error = e.message ?: "Ошибка загрузки стран",
                )
            }
        }
    }

    fun getHolidayById(holidayId: String): Holiday? {
        val holiday = findHolidayInMemory(holidayId)
        if (holiday != null) {
            detailHolidayCache[holidayId] = holiday
            return holiday
        }
        return detailHolidayCache[holidayId]
    }

    private fun findHolidayInMemory(holidayId: String): Holiday? {
        val api = holidaysApiState.value
        val favs = favouritesOutcome.value.getOrElse { emptyList() }
        val fromApi = (api as? ApiHolidaysState.Success)?.holidays
        fromApi?.firstOrNull { it.id == holidayId }?.let { return it }
        favs.firstOrNull { it.id == holidayId }?.let { return it }
        val listState = listPresentation.value
        return (listState as? HolidayListState.Success)
            ?.holidays
            ?.firstOrNull { it.id == holidayId }
    }
}
