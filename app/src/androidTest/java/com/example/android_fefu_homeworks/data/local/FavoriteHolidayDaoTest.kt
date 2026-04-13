package com.example.android_fefu_homeworks.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteHolidayDaoTest {

    private lateinit var db: HolidayDatabase
    private lateinit var dao: FavoriteHolidayDao

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, HolidayDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.favoriteHolidayDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_thenGetAll_returnsHolidayWithTypesAndCounties() = kotlinx.coroutines.test.runTest {
        val holiday = FavouriteHolidayEntity(
            id = "id-1",
            date = "2026-01-01",
            localName = "Новый год",
            name = "New Year's Day",
            countryCode = "RU",
            global = true,
        )
        val types = listOf(
            FavouritesTypesEntity(holidayId = "id-1", name = "Public"),
            FavouritesTypesEntity(holidayId = "id-1", name = "Bank"),
        )
        val counties = listOf(
            FavouritesCountiesEntity(holidayId = "id-1", name = "RU-PRI"),
        )

        dao.insert(holiday, types, counties)

        val all = dao.getAll()
        assertEquals(1, all.size)
        assertEquals("id-1", all.first().holiday.id)
        assertEquals(setOf("Public", "Bank"), all.first().types.map { it.name }.toSet())
        assertEquals(listOf("RU-PRI"), all.first().counties.map { it.name })
    }

    @Test
    fun deleteById_cascadesToTypesAndCounties() = kotlinx.coroutines.test.runTest {
        val holiday = FavouriteHolidayEntity(
            id = "id-2",
            date = "2026-02-01",
            localName = "x",
            name = "x",
            countryCode = "RU",
            global = false,
        )
        val types = listOf(
            FavouritesTypesEntity(holidayId = "id-2", name = "Public"),
        )
        val counties = listOf(
            FavouritesCountiesEntity(holidayId = "id-2", name = "RU-PRI"),
            FavouritesCountiesEntity(holidayId = "id-2", name = "RU-MOW"),
        )

        dao.insert(holiday, types, counties)
        dao.deleteById("id-2")

        assertTrue(dao.getAll().isEmpty())

        val typesLeft = scalarCount("SELECT COUNT(*) FROM favourite_types WHERE holidayId = 'id-2'")
        val countiesLeft = scalarCount("SELECT COUNT(*) FROM favourite_counties WHERE holidayId = 'id-2'")
        assertEquals(0, typesLeft)
        assertEquals(0, countiesLeft)
    }

    private fun scalarCount(sql: String): Int {
        val cursor = db.openHelper.readableDatabase.query(sql)
        cursor.use {
            it.moveToFirst()
            return it.getInt(0)
        }
    }
}

