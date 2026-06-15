package com.example.android_fefu_homeworks.ui.widget

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.android_fefu_homeworks.model.Holiday
import com.example.android_fefu_homeworks.model.Note
import com.example.android_fefu_homeworks.model.occursOn
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarWidget(
    year: Int,
    month: Int, // 1-12
    selectedDate: LocalDate?,
    holidays: List<Holiday>,
    notes: Map<String, List<Note>>,
    onDayClick: (LocalDate) -> Unit
) {
    val yearMonth = YearMonth.of(year, month)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value // 1 (Mon) to 7 (Sun)
    val today = LocalDate.now()
    
    val holidaysByDay = holidays.filter { 
        try {
            val date = LocalDate.parse(it.date)
            date.monthValue == month && date.year == year
        } catch (e: Exception) {
            false
        }
    }.groupBy { LocalDate.parse(it.date).dayOfMonth }

    val allNotesList = remember(notes) { notes.values.flatten() }

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val totalCells = (firstDayOfWeek - 1) + daysInMonth
        val rows = if (totalCells % 7 == 0) totalCells / 7 else (totalCells / 7) + 1
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 1..7) {
                        val dayIndex = row * 7 + col
                        val dayOfMonth = dayIndex - (firstDayOfWeek - 1)
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayOfMonth in 1..daysInMonth) {
                                val currentDate = yearMonth.atDay(dayOfMonth)
                                
                                val dayNotes = allNotesList.filter { note ->
                                    note.occursOn(currentDate)
                                }

                                val isSelected = currentDate == selectedDate
                                val isToday = currentDate == today
                                val hasHoliday = holidaysByDay.containsKey(dayOfMonth)
                                
                                DayCell(
                                    day = dayOfMonth,
                                    isHoliday = hasHoliday,
                                    notes = dayNotes,
                                    isSelected = isSelected,
                                    isToday = isToday,
                                    onClick = { onDayClick(currentDate) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(
    day: Int, 
    isHoliday: Boolean, 
    notes: List<Note>, 
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isHoliday -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color.Transparent
    }

    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        isToday -> MaterialTheme.colorScheme.primary
        isHoliday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(backgroundColor)
            .run {
                if (isToday && !isSelected) {
                    this.border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                } else this
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected || isToday || isHoliday) FontWeight.Bold else FontWeight.Normal
                ),
                color = contentColor
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(top = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isHoliday) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary)
                    )
                }
                
                notes.distinctBy { it.category }.take(3).forEach { note ->
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(Color(note.category.colorHex))
                    )
                }
            }
        }
    }
}
