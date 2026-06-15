package com.example.android_fefu_homeworks.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.android_fefu_homeworks.data.AppTheme
import com.example.android_fefu_homeworks.model.Country
import com.example.android_fefu_homeworks.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Theme Section
            Column {
                Text(
                    text = "Тема оформления",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(Modifier.selectableGroup()) {
                    AppTheme.entries.forEach { theme ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .selectable(
                                    selected = (theme == uiState.theme),
                                    onClick = { viewModel.setTheme(theme) },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (theme == uiState.theme),
                                onClick = null
                            )
                            Text(
                                text = when (theme) {
                                    AppTheme.LIGHT -> "Светлая"
                                    AppTheme.DARK -> "Темная"
                                    AppTheme.SYSTEM -> "Системная"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // Country Section
            Column {
                Text(
                    text = "Регион для праздников",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                CountrySelector(
                    countries = uiState.countries,
                    selectedCountryCode = uiState.selectedCountryCode,
                    onCountrySelected = { viewModel.setCountry(it) },
                    isLoading = uiState.isLoadingCountries
                )
            }
        }
    }
}

@Composable
fun CountrySelector(
    countries: List<Country>,
    selectedCountryCode: String?,
    onCountrySelected: (String) -> Unit,
    isLoading: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCountry = countries.find { it.countryCode == selectedCountryCode }

    OutlinedButton(
        onClick = { if (!isLoading) expanded = true },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        enabled = !isLoading,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = selectedCountry?.name ?: if (isLoading) "Загрузка..." else "Выберите вашу страну",
                textAlign = TextAlign.Start
            )
        }
        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
    }

    if (expanded) {
        Dialog(onDismissRequest = { expanded = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.8f),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 4.dp
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Выберите страну", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    LazyColumn(Modifier.weight(1f)) {
                        itemsIndexed(countries) { _, country ->
                            ListItem(
                                headlineContent = { Text(country.name) },
                                modifier = Modifier.clickable {
                                    onCountrySelected(country.countryCode)
                                    expanded = false
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = if (country.countryCode == selectedCountryCode) 
                                        MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent
                                )
                            )
                        }
                    }
                    TextButton(onClick = { expanded = false }, Modifier.align(Alignment.End)) {
                        Text("Закрыть")
                    }
                }
            }
        }
    }
}
