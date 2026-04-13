package com.example.android_fefu_homeworks

import com.example.android_fefu_homeworks.data.HolidayRepository
import com.example.android_fefu_homeworks.data.HolidayRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideHolidayRepository(impl: HolidayRepositoryImpl): HolidayRepository = impl
}
