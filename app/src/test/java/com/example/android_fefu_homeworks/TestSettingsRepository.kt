package com.example.android_fefu_homeworks

import com.example.android_fefu_homeworks.data.AppTheme
import com.example.android_fefu_homeworks.data.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

fun fakeSettingsRepository(
    initialCountryCode: String? = null,
    theme: AppTheme = AppTheme.SYSTEM,
): SettingsRepository {
    val countryFlow = MutableStateFlow(initialCountryCode)
    val mock = mockk<SettingsRepository>(relaxed = true)
    every { mock.selectedCountryCode } returns countryFlow.asStateFlow()
    every { mock.appTheme } returns MutableStateFlow(theme).asStateFlow()
    return mock
}
