package com.example.android_fefu_homeworks.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.MaterialTheme
import com.example.android_fefu_homeworks.model.Holiday
import com.example.android_fefu_homeworks.model.HolidayFilter
import com.example.android_fefu_homeworks.ui.viewmodel.HolidayListState
import com.example.android_fefu_homeworks.ui.viewmodel.HolidayUiState
import com.example.android_fefu_homeworks.ui.widget.HolidayCard

@Composable
fun HolidayListScreen(
    state: HolidayUiState,
    onQueryChange: (String) -> Unit,
    onCountryChange: (String) -> Unit,
    onYearChange: (Int) -> Unit,
    onFilterChange: (HolidayFilter) -> Unit,
    onToggleFavourite: (String) -> Unit,
    onHolidayClick: (String) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit
) {
    var yearInput by remember { mutableStateOf(state.selectedYear.toString()) }
    
    LaunchedEffect(state.selectedYear) {
        yearInput = state.selectedYear.toString()
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                    Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Календарь праздников",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    IconButton(
                        onClick = onRefresh,
                        enabled = state.selectedCountryCode != null && state.listState !is com.example.android_fefu_homeworks.ui.viewmodel.HolidayListState.Loading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Country selector
            CountrySelector(
                countries = state.countries,
                selectedCountryCode = state.selectedCountryCode,
                onCountrySelected = onCountryChange,
                isLoading = state.isLoadingCountries
            )

            // Year selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val newYear = state.selectedYear - 1
                        if (newYear >= 1900) {
                            onYearChange(newYear)
                        }
                    },
                    enabled = state.selectedYear > 1900
                ) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Уменьшить год")
                }
                OutlinedTextField(
                    value = yearInput,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            yearInput = newValue
                            newValue.toIntOrNull()?.let { year ->
                                if (year >= 1900 && year <= 2100) {
                                    onYearChange(year)
                                }
                            }
                        }
                    },
                    label = { Text("Год") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
                IconButton(
                    onClick = {
                        val newYear = state.selectedYear + 1
                        if (newYear <= 2100) {
                            onYearChange(newYear)
                        }
                    },
                    enabled = state.selectedYear < 2100
                ) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Увеличить год")
                }
            }

            // Search field
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth()
                    .padding(6.dp),
                label = { Text("Поиск праздников") },
                enabled = state.selectedCountryCode != null,
                singleLine = true
            )

            // Filter buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { onFilterChange(HolidayFilter.ALL) },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (state.filter == HolidayFilter.ALL) 
                            MaterialTheme.colorScheme.surfaceVariant 
                        else 
                            Color.Transparent
                    )
                ) {
                    Text(if (state.filter == HolidayFilter.ALL) "-Все-" else "Все")
                }
                TextButton(
                    onClick = { onFilterChange(HolidayFilter.FAVOURITES) },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (state.filter == HolidayFilter.FAVOURITES) 
                            MaterialTheme.colorScheme.surfaceVariant 
                        else 
                            Color.Transparent
                    )
                ) {
                    Text(if (state.filter == HolidayFilter.FAVOURITES) "-Избранное-" else "Избранное")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content based on state
            when (val listState = state.listState) {
                is HolidayListState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is HolidayListState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Ошибка: ${listState.message}",
                                textAlign = TextAlign.Center
                            )
                            Button(onClick = onRetry) {
                                Text("Повторить")
                            }
                        }
                    }
                }
                is HolidayListState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.selectedCountryCode == null) {
                                "Выберите страну для просмотра праздников"
                            } else if (state.filter == HolidayFilter.FAVOURITES) {
                                "Нет избранных праздников"
                            } else if (state.query.isNotBlank()) {
                                "Праздники не найдены"
                            } else {
                                "Праздники недоступны"
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is HolidayListState.Success -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            items = listState.holidays,
                            key = { index, holiday -> "${holiday.id}_$index" }
                        ) { _, holiday ->
                            HolidayCard(
                                holiday = holiday,
                                isFavourite = holiday.id in state.favourites,
                                onToggleFavourite = { onToggleFavourite(holiday.id) },
                                onClick = { onHolidayClick(holiday.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CountrySelector(
    countries: List<com.example.android_fefu_homeworks.model.Country>,
    selectedCountryCode: String?,
    onCountrySelected: (String) -> Unit,
    isLoading: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCountry = countries.find { it.countryCode == selectedCountryCode }

    OutlinedButton(
        onClick = { if (!isLoading) expanded = true },
        modifier = Modifier.fillMaxWidth()
            .height(55.dp)
            .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(
            topStart = 6.dp,
            topEnd = 6.dp,
            bottomEnd = 6.dp,
            bottomStart = 6.dp
        ),
        enabled = !isLoading,
    ) {
        Text(
            text = selectedCountry?.name ?: "Выберите страну",
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            textAlign = TextAlign.Start
        )
        Icon(
            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = if (expanded) "Свернуть" else "Развернуть"
        )
    }

    if (expanded) {
        Dialog(
            onDismissRequest = { expanded = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Выберите страну",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(countries) { country ->
                            val selected = country.countryCode == selectedCountryCode
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onCountrySelected(country.countryCode)
                                        expanded = false
                                    },
                                color = if (selected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = country.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Закрыть")
                    }
                }
            }
        }
    }
}
