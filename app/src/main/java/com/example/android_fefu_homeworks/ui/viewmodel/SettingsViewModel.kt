package com.example.android_fefu_homeworks.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_fefu_homeworks.data.AppTheme
import com.example.android_fefu_homeworks.data.HolidayRepository
import com.example.android_fefu_homeworks.data.SettingsRepository
import com.example.android_fefu_homeworks.model.Country
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val selectedCountryCode: String? = null,
    val countries: List<Country> = emptyList(),
    val isLoadingCountries: Boolean = false,
    val isReady: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val holidayRepository: HolidayRepository
) : ViewModel() {

    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    private val _isLoadingCountries = MutableStateFlow(false)

    init {
        loadCountries()
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.appTheme,
        settingsRepository.selectedCountryCode,
        _countries,
        _isLoadingCountries
    ) { theme, countryCode, countries, loading ->
        SettingsUiState(
            theme = theme,
            selectedCountryCode = countryCode,
            countries = countries,
            isLoadingCountries = loading,
            isReady = true
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.saveTheme(theme)
        }
    }

    fun setCountry(countryCode: String) {
        viewModelScope.launch {
            settingsRepository.saveCountryCode(countryCode)
        }
    }

    private fun loadCountries() {
        viewModelScope.launch {
            _isLoadingCountries.value = true
            try {
                _countries.value = holidayRepository.getAvailableCountries()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoadingCountries.value = false
            }
        }
    }
}
