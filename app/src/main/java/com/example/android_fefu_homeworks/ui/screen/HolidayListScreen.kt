package com.example.android_fefu_homeworks.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.android_fefu_homeworks.ui.viewmodel.HolidayListState
import com.example.android_fefu_homeworks.ui.viewmodel.HolidayUiState
import com.example.android_fefu_homeworks.ui.widget.CalendarWidget
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidayListScreen(
    state: HolidayUiState,
    onCountryChange: (String) -> Unit,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onGoToToday: () -> Unit,
    onToggleShowOnlyNotes: (Boolean) -> Unit,
    onHolidayClick: (String) -> Unit,
    onAddNoteClick: (LocalDate) -> Unit,
    onNoteClick: (String) -> Unit,
    onToggleNoteFavourite: (String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мой Календарь") },
                actions = {
                    // Кнопка-переключатель режима "Только заметки"
                    IconToggleButton(
                        checked = state.showOnlyNotes,
                        onCheckedChange = onToggleShowOnlyNotes
                    ) {
                        Icon(
                            imageVector = if (state.showOnlyNotes) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (state.showOnlyNotes) "Показать все события" else "Только избранные заметки",
                            tint = if (state.showOnlyNotes) MaterialTheme.colorScheme.error else LocalContentColor.current
                        )
                    }
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
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val monthName = remember(state.selectedMonth) {
                    java.time.Month.of(state.selectedMonth + 1)
                        .getDisplayName(TextStyle.FULL, Locale("ru"))
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() }
                }

                Row(
                    modifier = Modifier.weight(1.5f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onMonthChange(state.selectedMonth - 1) }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Предыдущий месяц")
                    }
                    Text(
                        text = monthName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { onMonthChange(state.selectedMonth + 1) }) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Следующий месяц")
                    }
                }

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onYearChange(state.selectedYear - 1) }) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Предыдущий год")
                    }
                    Text(
                        text = state.selectedYear.toString(),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { onYearChange(state.selectedYear + 1) }) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Следующий год")
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
            
            // Фильтрация событий для отображения в списке
            val dayHolidays = if (state.showOnlyNotes) {
                emptyList() // В режиме заметок праздники не показываем
            } else {
                holidays.filter { it.date == dateStr }
            }

            val dayNotes = if (state.showOnlyNotes) {
                // Показываем все избранные заметки
                state.notes.values.flatten().filter { it.isFavourite }
            } else {
                state.notes[dateStr] ?: emptyList()
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (state.showOnlyNotes) "Избранные заметки" else "${state.selectedDate.dayOfMonth} ${state.selectedDate.month.getDisplayName(TextStyle.FULL, Locale("ru"))}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
                if (!state.showOnlyNotes && state.selectedDate == LocalDate.now()) {
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
                            if (state.showOnlyNotes) "У вас пока нет избранных заметок" else "Событий не запланировано",
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
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        )
                    }
                }

                itemsIndexed(dayNotes) { _, note ->
                    Card(
                        onClick = { onNoteClick(note.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                    ) {
                        ListItem(
                            headlineContent = { Text(note.text) },
                            supportingContent = {
                                Column {
                                    if (note.description.isNotBlank()) {
                                        Text(note.description, maxLines = 1)
                                    }
                                    if (state.showOnlyNotes) {
                                        Text(
                                            text = note.date,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { onToggleNoteFavourite(note.id) }) {
                                        Icon(
                                            if (note.isFavourite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = null,
                                            tint = if (note.isFavourite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    IconButton(onClick = { onDeleteNote(note.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Удалить", modifier = Modifier.size(20.dp))
                                    }
                                }
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        )
                    }
                }

                if (!state.showOnlyNotes) {
                    item {
                        Button(
                            onClick = { onAddNoteClick(state.selectedDate) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Добавить событие или заметку")
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
