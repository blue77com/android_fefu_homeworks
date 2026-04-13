package com.example.android_fefu_homeworks.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android_fefu_homeworks.data.local.HolidayDatabase
import com.example.android_fefu_homeworks.data.remote.NagerApi
import com.example.android_fefu_homeworks.model.Holiday
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HolidayRepositoryRoomIntegrationTest {

    private lateinit var db: HolidayDatabase
    private lateinit var api: NagerApi

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, HolidayDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        api = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun addFavorite_thenGetFavourites_roundTripsDomainModel() = runTest {
        val repo = HolidayRepositoryImpl(api = api, favoriteHolidayDao = db.favoriteHolidayDao())
        val holiday = Holiday(
            date = "2026-01-01",
            localName = "Новый год",
            name = "New Year's Day",
            countryCode = "RU",
            global = true,
            counties = listOf("RU-PRI"),
            types = listOf("Public", "Bank"),
        )

        repo.addFavorite(holiday)
        val favs = repo.getFavourites()

        assertEquals(1, favs.size)
        assertEquals(holiday.id, favs.first().id)
        assertEquals(holiday.countryCode, favs.first().countryCode)
        assertEquals(setOf("Public", "Bank"), favs.first().types.toSet())
        assertEquals(listOf("RU-PRI"), favs.first().counties)
    }
}

