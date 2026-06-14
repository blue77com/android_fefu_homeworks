package com.example.android_fefu_homeworks.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android_fefu_homeworks.ui.viewmodel.HolidayListState
import com.example.android_fefu_homeworks.ui.viewmodel.HolidayUiState
import com.example.android_fefu_homeworks.ui.widget.CalendarWidget
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidayListScreen(
    state: HolidayUiState,
    onCountryChange: (String) -> Unit,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onGoToToday: () -> Unit,
    onToggleFavourite: (String) -> Unit,
    onHolidayClick: (String) -> Unit,
    onAddNote: (String, String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onDismissFavouriteActionError: () -> Unit,
) {
    var noteText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мой Календарь") },
                actions = {
                    IconButton(onClick = onGoToToday) {
                        Icon(Icons.Default.DateRange, contentDescription = "Сегодня")
                    }
                    IconButton(
                        onClick = onRefresh,
                        enabled = state.selectedCountryCode != null && state.listState !is HolidayListState.Loading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Селектор страны
            CountrySelector(
                countries = state.countries,
                selectedCountryCode = state.selectedCountryCode,
                onCountrySelected = onCountryChange,
                isLoading = state.isLoadingCountries
            )

            // Навигация по датам
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonthSelector(
                    modifier = Modifier.weight(1.5f),
                    selectedMonth = state.selectedMonth,
                    onMonthSelected = onMonthChange
                )

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onYearChange(state.selectedYear - 1) }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null)
                    }
                    Text(
                        text = state.selectedYear.toString(),
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { onYearChange(state.selectedYear + 1) }) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }

            val holidays = (state.listState as? HolidayListState.Success)?.holidays ?: emptyList()
            
            // Виджет календаря
            CalendarWidget(
                year = state.selectedYear,
                month = state.selectedMonth + 1,
                selectedDate = state.selectedDate,
                holidays = holidays,
                notes = state.notes,
                onDayClick = onDateSelected
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            
            // Список событий на выбранный день
            val dateStr = state.selectedDate.toString()
            val dayHolidays = holidays.filter { it.date == dateStr }
            val dayNotes = state.notes[dateStr] ?: emptyList()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.selectedDate.dayOfMonth} ${state.selectedDate.month.getDisplayName(TextStyle.FULL, Locale("ru"))}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
                if (state.selectedDate == LocalDate.now()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "Сегодня",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (dayHolidays.isEmpty() && dayNotes.isEmpty()) {
                    item {
                        Text(
                            "Событий не запланировано",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }

                itemsIndexed(dayHolidays) { _, holiday ->
                    Card(
                        onClick = { onHolidayClick(holiday.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        ListItem(
                            headlineContent = { Text(holiday.localName, fontWeight = FontWeight.Bold) },
                            supportingContent = { Text(holiday.name) },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            },
                            trailingContent = {
                                val isFav = holiday.id in state.favourites
                                IconButton(onClick = { onToggleFavourite(holiday.id) }) {
                                    Icon(
                                        if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = null,
                                        tint = if (isFav) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        )
                    }
                }

                itemsIndexed(dayNotes) { _, note ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                    ) {
                        ListItem(
                            headlineContent = { Text(note.text) },
                            trailingContent = {
                                IconButton(onClick = { onDeleteNote(note.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Удалить", modifier = Modifier.size(20.dp))
                                }
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = { Text("Что планируете на этот день?") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        trailingIcon = {
                            if (noteText.isNotBlank()) {
                                IconButton(onClick = {
                                    onAddNote(dateStr, noteText)
                                    noteText = ""
                                }) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Добавить", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthSelector(
    modifier: Modifier = Modifier,
    selectedMonth: Int,
    onMonthSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val months = (0..11).map { 
        java.time.Month.of(it + 1).getDisplayName(TextStyle.FULL, Locale("ru"))
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = months[selectedMonth],
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            months.forEachIndexed { index, name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onMonthSelected(index)
                        expanded = false
                    }
                )
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
                text = selectedCountry?.name ?: "Выберите вашу страну",
                textAlign = androidx.compose.ui.text.style.TextAlign.Start
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
