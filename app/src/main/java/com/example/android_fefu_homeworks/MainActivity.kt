package com.example.android_fefu_homeworks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.android_fefu_homeworks.ui.theme.HolidayBrowserTheme
import com.example.android_fefu_homeworks.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Устанавливаем SplashScreen до super.onCreate()
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Удерживаем SplashScreen пока тема не загружена из DataStore
        splashScreen.setKeepOnScreenCondition {
            !settingsViewModel.uiState.value.isReady
        }

        enableEdgeToEdge()
        setContent {
            val uiState by settingsViewModel.uiState.collectAsState()
            
            HolidayBrowserTheme(appTheme = uiState.theme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    HolidayApp()
                }
            }
        }
    }
}
