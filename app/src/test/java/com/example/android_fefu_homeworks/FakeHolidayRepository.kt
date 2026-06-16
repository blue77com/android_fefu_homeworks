package com.example.android_fefu_homeworks

import com.example.android_fefu_homeworks.data.HolidayRepository
import com.example.android_fefu_homeworks.model.Country
import com.example.android_fefu_homeworks.model.Holiday
import com.example.android_fefu_homeworks.model.Note
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeHolidayRepository(
    initialCountries: List<Country> = emptyList(),
    initialNotes: List<Note> = emptyList(),
    var publicHolidaysProvider: suspend (Int, String) -> List<Holiday> = { _, _ -> emptyList() },
) : HolidayRepository {

    private val countriesFlow = MutableStateFlow(initialCountries)
    private val notesFlow = MutableStateFlow(initialNotes)
    private val holidaysStore = mutableMapOf<Pair<Int, String>, MutableStateFlow<List<Holiday>>>()

    var refreshCountriesCalls: Int = 0
        private set
    var refreshPublicHolidaysCalls: Int = 0
        private set

    var failCountries: Boolean = false
    var failPublicHolidays: Boolean = false

    override fun observeCountries(): Flow<List<Country>> = countriesFlow.asStateFlow()

    override suspend fun refreshCountries() {
        refreshCountriesCalls++
        if (failCountries) {
            throw IOException("countries failed")
        }
    }

    override fun observePublicHolidays(year: Int, countryCode: String): Flow<List<Holiday>> {
        val key = year to countryCode
        return holidaysStore.getOrPut(key) { MutableStateFlow(emptyList()) }.asStateFlow()
    }

    override suspend fun refreshPublicHolidays(year: Int, countryCode: String) {
        refreshPublicHolidaysCalls++
        if (failPublicHolidays) {
            throw IOException("holidays failed")
        }
        val holidays = publicHolidaysProvider(year, countryCode)
        holidaysStore.getOrPut(year to countryCode) { MutableStateFlow(emptyList()) }.value = holidays
    }

    override fun observeNotes(): Flow<List<Note>> = notesFlow.asStateFlow()

    override suspend fun addNote(note: Note) {
        notesFlow.value = notesFlow.value + note
    }

    override suspend fun updateNote(note: Note) {
        notesFlow.value = notesFlow.value.map { if (it.id == note.id) note else it }
    }

    override suspend fun toggleNoteFavourite(id: String) {
        notesFlow.value = notesFlow.value.map { note ->
            if (note.id == id) note.copy(isFavourite = !note.isFavourite) else note
        }
    }

    override suspend fun getNoteById(id: String): Note? = notesFlow.value.find { it.id == id }

    override suspend fun deleteNote(id: String) {
        notesFlow.value = notesFlow.value.filter { it.id != id }
    }
}
