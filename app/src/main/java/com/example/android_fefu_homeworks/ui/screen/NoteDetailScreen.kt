package com.example.android_fefu_homeworks.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.android_fefu_homeworks.model.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteDetailScreen(
    note: Note?,
    initialDate: String,
    onSave: (Note) -> Unit,
    onBackClick: () -> Unit
) {
    var title by remember(note) { mutableStateOf(note?.text ?: "") }
    var description by remember(note) { mutableStateOf(note?.description ?: "") }
    var isFavourite by remember(note) { mutableStateOf(note?.isFavourite ?: false) }
    var category by remember(note) { mutableStateOf(note?.category ?: NoteCategory.PERSONAL) }
    var repeatMode by remember(note) { mutableStateOf(note?.repeatMode ?: RepeatMode.NONE) }
    var checklist by remember(note) { mutableStateOf(note?.checklist ?: emptyList<ChecklistItem>()) }

    val currentDate = LocalDate.now()
    val noteDate = LocalDate.parse(initialDate)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (note == null) "Новая заметка" else "Редактировать") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { isFavourite = !isFavourite }) {
                        Icon(
                            if (isFavourite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Избранное",
                            tint = if (isFavourite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Заголовок") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Column(modifier = Modifier.animateContentSize()) {
                    if (category == NoteCategory.DEADLINE) {
                        val daysDiff = ChronoUnit.DAYS.between(currentDate, noteDate)
                        val deadlineText = when {
                            daysDiff > 0 -> "Осталось дней: $daysDiff"
                            daysDiff < 0L -> "Просрочено на ${-daysDiff} дн."
                            else -> "Дедлайн сегодня!"
                        }
                        val color = if (daysDiff >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        
                        Surface(
                            color = color.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = color)
                                Text(
                                    text = deadlineText,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = color,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "Категория",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NoteCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat.displayName) },
                                leadingIcon = {
                                    if (category == cat) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(Color(cat.colorHex), CircleShape)
                                        )
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(cat.colorHex).copy(alpha = 0.2f),
                                    selectedLabelColor = Color(cat.colorHex),
                                    selectedLeadingIconColor = Color(cat.colorHex)
                                )
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Повторение",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedCard(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        ListItem(
                            headlineContent = { Text(repeatMode.displayName) },
                            leadingContent = { Icon(Icons.Default.Refresh, null) },
                            trailingContent = { Icon(Icons.Default.ArrowDropDown, null) }
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        RepeatMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.displayName) },
                                onClick = {
                                    repeatMode = mode
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Чек-лист",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = {
                        checklist = checklist + ChecklistItem(text = "")
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Добавить")
                    }
                }
            }

            itemsIndexed(checklist) { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = { checked ->
                            checklist = checklist.toMutableList().also {
                                it[index] = it[index].copy(isChecked = checked)
                            }
                        }
                    )
                    TextField(
                        value = item.text,
                        onValueChange = { newText ->
                            checklist = checklist.toMutableList().also {
                                it[index] = it[index].copy(text = newText)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Что нужно сделать?") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    IconButton(onClick = {
                        checklist = checklist.toMutableList().also { it.removeAt(index) }
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Удалить", modifier = Modifier.size(20.dp))
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Заметки и описание") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            item {
                Button(
                    onClick = {
                        val finalNote = note?.copy(
                            text = title,
                            description = description,
                            isFavourite = isFavourite,
                            category = category,
                            repeatMode = repeatMode,
                            checklist = checklist
                        ) ?: Note(
                            date = initialDate,
                            text = title,
                            description = description,
                            isFavourite = isFavourite,
                            category = category,
                            repeatMode = repeatMode,
                            checklist = checklist
                        )
                        onSave(finalNote)
                        onBackClick()
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    enabled = title.isNotBlank()
                ) {
                    Text("Сохранить")
                }
            }
        }
    }
}
