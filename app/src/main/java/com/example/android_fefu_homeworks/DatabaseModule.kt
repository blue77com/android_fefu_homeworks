package com.example.android_fefu_homeworks

import android.content.Context
import androidx.room.Room
import com.example.android_fefu_homeworks.data.local.CountryDao
import com.example.android_fefu_homeworks.data.local.FavoriteHolidayDao
import com.example.android_fefu_homeworks.data.local.HolidayCacheDao
import com.example.android_fefu_homeworks.data.local.HolidayDatabase
import com.example.android_fefu_homeworks.data.local.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HolidayDatabase =
        Room.databaseBuilder(
            context,
            klass = HolidayDatabase::class.java,
            name = "holiday_browser.db"
        )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun providesFavoriteDao(db: HolidayDatabase): FavoriteHolidayDao =
        db.favoriteHolidayDao()

    @Provides
    @Singleton
    fun providesNoteDao(db: HolidayDatabase): NoteDao =
        db.noteDao()

    @Provides
    @Singleton
    fun providesHolidayCacheDao(db: HolidayDatabase): HolidayCacheDao =
        db.holidayCacheDao()

    @Provides
    @Singleton
    fun providesCountryDao(db: HolidayDatabase): CountryDao =
        db.countryDao()
}