package com.example.android_fefu_homeworks.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val countryCodeKey = stringPreferencesKey("selected_country_code")

    val selectedCountryCode: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[countryCodeKey] }

    suspend fun saveCountryCode(code: String) {
        context.dataStore.edit { preferences ->
            preferences[countryCodeKey] = code
        }
    }
}
