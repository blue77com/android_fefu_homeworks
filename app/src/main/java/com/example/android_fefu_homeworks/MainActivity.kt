package com.example.android_fefu_homeworks

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.android_fefu_homeworks.ui.theme.HolidayBrowserTheme
import com.example.android_fefu_homeworks.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* разрешение обрабатывается системой */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()

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

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
