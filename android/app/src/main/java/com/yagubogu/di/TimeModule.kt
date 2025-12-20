package com.yagubogu.di

import com.yagubogu.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TimeModule {
    @Provides
    @Singleton
    fun provideClock(): Clock {
        val kstZone = ZoneId.of("Asia/Seoul")
        return if (BuildConfig.DEBUG) {
            val fixedDateTime = ZonedDateTime.of(2025, 9, 24, 18, 0, 0, 0, kstZone)
            Clock.fixed(fixedDateTime.toInstant(), kstZone)
        } else {
            Clock.system(kstZone)
        }
    }
}
