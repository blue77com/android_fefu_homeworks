package com.example.android_fefu_homeworks.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import com.example.android_fefu_homeworks.data.local.FavoriteHolidayDao
import com.example.android_fefu_homeworks.data.local.FavouriteHolidayEntity
import com.example.android_fefu_homeworks.data.local.FavouritesCountiesEntity
import com.example.android_fefu_homeworks.data.local.FavouritesTypesEntity
import com.example.android_fefu_homeworks.data.local.HolidayCacheDao
import com.example.android_fefu_homeworks.data.local.NoteDao
import com.example.android_fefu_homeworks.data.local.CountryDao
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
    suspend fun addFavorite(Holiday: Holiday)
    suspend fun removeFavorite(id: String)
    fun observeFavourites(): Flow<List<Holiday>>

    suspend fun getAvailableCountries(forceRefresh: Boolean = false): List<Country>
    suspend fun getPublicHolidays(year: Int, countryCode: String, forceRefresh: Boolean = false) : List<Holiday>

    // Notes
    fun observeNotes(): Flow<List<Note>>
    suspend fun addNote(note: Note)
    suspend fun deleteNote(id: String)
}

@Singleton
class HolidayRepositoryImpl @Inject constructor(
    private val api: NagerApi,
    private val favoriteHolidayDao: FavoriteHolidayDao,
    private val noteDao: NoteDao,
    private val holidayCacheDao: HolidayCacheDao,
    private val countryDao: CountryDao,
) : HolidayRepository{

    override suspend fun addFavorite(Holiday: Holiday) = withContext(Dispatchers.IO)
    {
        val holidayEntity = FavouriteHolidayEntity(
            id =  Holiday.id,
            date = Holiday.date,
            localName = Holiday.localName ,
            name = Holiday.name,
            countryCode = Holiday.countryCode,
            global = Holiday.global
        )

        val typeEntities = Holiday.types?.map { typeName ->
            FavouritesTypesEntity(
                holidayId = Holiday.id,
                name = typeName
            )
        } ?: emptyList()

        val countyEntities = Holiday.counties?.map { countyCode ->
            FavouritesCountiesEntity(
                holidayId = Holiday.id,
                name = countyCode
            )
        } ?: emptyList()

        favoriteHolidayDao.insert(holidayEntity, typeEntities, countyEntities)
    }

    override fun observeFavourites(): Flow<List<Holiday>> =
        favoriteHolidayDao.observeAll()
            .map { rows -> rows.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    override suspend fun removeFavorite(id: String) = withContext(Dispatchers.IO){
        favoriteHolidayDao.deleteById(id)
    }

    override suspend fun getAvailableCountries(forceRefresh: Boolean): List<Country> = withContext(Dispatchers.IO) {
        if (!forceRefresh) {
            val cachedCountries = countryDao.getAllCountries()
            if (cachedCountries.isNotEmpty()) {
                return@withContext cachedCountries.map { it.toDomain() }
            }
        }

        try {
            val remoteCountries = api.getAvailableCountries().map { it.toDomain() }
            countryDao.deleteAll()
            countryDao.insertCountries(remoteCountries.map { it.toEntity() })
            remoteCountries
        } catch (e: Exception) {
            val fallback = countryDao.getAllCountries()
            if (fallback.isNotEmpty()) fallback.map { it.toDomain() }
            else throw e
        }
    }
    
    override suspend fun getPublicHolidays(year: Int, countryCode: String, forceRefresh: Boolean): List<Holiday> = withContext(Dispatchers.IO) {
        if (!forceRefresh) {
            val cachedHolidays = holidayCacheDao.getHolidays(countryCode, year)
            if (cachedHolidays.isNotEmpty()) {
                return@withContext cachedHolidays.map { it.toDomain() }
            }
        }

        try {
            val remoteHolidays = api.getPublicHolidays(year, countryCode).map { it.toDomain() }
            if (forceRefresh) {
                holidayCacheDao.deleteHolidays(countryCode, year)
            }
            holidayCacheDao.insertHolidays(remoteHolidays.map { it.toCacheEntity(year) })
            remoteHolidays
        } catch (e: Exception) {
            val fallbackCache = holidayCacheDao.getHolidays(countryCode, year)
            if (fallbackCache.isNotEmpty()) fallbackCache.map { it.toDomain() }
            else throw e
        }
    }

    override fun observeNotes(): Flow<List<Note>> = 
        noteDao.getAllNotes()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    override suspend fun addNote(note: Note) = withContext(Dispatchers.IO) {
        noteDao.insertNote(note.toEntity())
    }

    override suspend fun deleteNote(id: String) = withContext(Dispatchers.IO) {
        noteDao.deleteNoteById(id)
    }
}
