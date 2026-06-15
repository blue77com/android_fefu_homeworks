package com.example.android_fefu_homeworks.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import com.example.android_fefu_homeworks.data.local.CountryDao
import com.example.android_fefu_homeworks.data.local.CountryEntity
import com.example.android_fefu_homeworks.data.local.HolidayCacheDao
import com.example.android_fefu_homeworks.data.local.NoteDao
import com.example.android_fefu_homeworks.data.local.toCacheEntity
import com.example.android_fefu_homeworks.data.local.toDomain
import com.example.android_fefu_homeworks.data.local.toEntity
import com.example.android_fefu_homeworks.data.remote.NagerApi
import com.example.android_fefu_homeworks.data.remote.toDomain
import com.example.android_fefu_homeworks.model.Country
import com.example.android_fefu_homeworks.model.Holiday
import com.example.android_fefu_homeworks.model.Note
import javax.inject.Inject
import javax.inject.Singleton

interface HolidayRepository {
    fun observeCountries(): Flow<List<Country>>
    suspend fun refreshCountries()
    fun observePublicHolidays(year: Int, countryCode: String): Flow<List<Holiday>>
    suspend fun refreshPublicHolidays(year: Int, countryCode: String)
    fun observeNotes(): Flow<List<Note>>
    suspend fun addNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun toggleNoteFavourite(id: String)
    suspend fun getNoteById(id: String): Note?
    suspend fun deleteNote(id: String)
}

@Singleton
class HolidayRepositoryImpl @Inject constructor(
    private val api: NagerApi,
    private val noteDao: NoteDao,
    private val holidayCacheDao: HolidayCacheDao,
    private val countryDao: CountryDao,
) : HolidayRepository {

    override fun observeCountries(): Flow<List<Country>> =
        countryDao.observeAllCountries()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    override suspend fun refreshCountries() = withContext(Dispatchers.IO) {
        val remoteDtos = api.getAvailableCountries()
        val entities = remoteDtos.map { dto ->
            CountryEntity(countryCode = dto.countryCode, name = dto.name)
        }
        if (entities.isNotEmpty()) {
            countryDao.deleteAll()
            countryDao.insertCountries(entities)
        }
    }

    override fun observePublicHolidays(year: Int, countryCode: String): Flow<List<Holiday>> =
        holidayCacheDao.observeHolidays(countryCode, year)
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    override suspend fun refreshPublicHolidays(year: Int, countryCode: String) = withContext(
        Dispatchers.IO
    ) {
        val remoteHolidays = api.getPublicHolidays(year, countryCode)
        val entities = remoteHolidays.map { dto ->
            dto.toDomain().toCacheEntity(year)
        }
        holidayCacheDao.deleteHolidays(countryCode, year)
        holidayCacheDao.insertHolidays(entities)
    }

    override fun observeNotes(): Flow<List<Note>> =
        noteDao.getAllNotes()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    override suspend fun addNote(note: Note) = withContext(Dispatchers.IO) {
        noteDao.insertNote(note.toEntity())
    }

    override suspend fun updateNote(note: Note) = withContext(Dispatchers.IO) {
        noteDao.updateNote(note.toEntity())
    }

    override suspend fun toggleNoteFavourite(id: String) = withContext(Dispatchers.IO) {
        noteDao.toggleNoteFavourite(id)
    }

    override suspend fun getNoteById(id: String): Note? = withContext(Dispatchers.IO) {
        noteDao.getNoteById(id)?.toDomain()
    }

    override suspend fun deleteNote(id: String) = withContext(Dispatchers.IO) {
        noteDao.deleteNoteById(id)
    }
}
