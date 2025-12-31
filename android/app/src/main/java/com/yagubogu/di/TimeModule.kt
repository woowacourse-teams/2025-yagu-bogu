package com.yagubogu.di

import com.yagubogu.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TimeModule {
    @Provides
    @Singleton
    fun provideClock(): Clock {
        val kstZone = ZoneId.of("Asia/Seoul")
        if (BuildConfig.DEBUG) {
            return runCatching {
                val localDateTime = LocalDateTime.parse(BuildConfig.DEBUG_FIXED_DATE)
                val fixedZonedDateTime = localDateTime.atZone(kstZone)

                Clock.fixed(fixedZonedDateTime.toInstant(), kstZone)
            }.getOrElse { e: Throwable ->
                Timber.e("디버그 Clock 생성 실패: $e")
                Clock.system(kstZone)
            }
        }

        return Clock.system(kstZone)
    }
}
