package com.example.android_fefu_homeworks.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android_fefu_homeworks.data.local.HolidayDatabase
import com.example.android_fefu_homeworks.data.remote.NagerApi
import com.example.android_fefu_homeworks.model.Note
import io.mockk.mockk
import kotlinx.coroutines.flow.first
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
    fun addNote_thenObserveNotes_roundTripsDomainModel() = runTest {
        val repo = HolidayRepositoryImpl(
            api = api,
            noteDao = db.noteDao(),
            holidayCacheDao = db.holidayCacheDao(),
            countryDao = db.countryDao(),
        )
        val note = Note(
            id = "note-1",
            date = "2026-06-15",
            text = "Важная заметка",
            description = "Описание",
            isFavourite = true,
        )

        repo.addNote(note)
        val notes = repo.observeNotes().first()

        assertEquals(1, notes.size)
        assertEquals(note.id, notes.first().id)
        assertEquals(note.text, notes.first().text)
        assertEquals(true, notes.first().isFavourite)
    }
}
