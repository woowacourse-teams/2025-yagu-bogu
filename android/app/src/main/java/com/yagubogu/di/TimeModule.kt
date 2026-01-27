package com.yagubogu.di

import co.touchlab.kermit.Logger
import com.yagubogu.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TimeModule {
    @Provides
    @Singleton
    fun provideClock(kermitLogger: Logger): Clock {
        val logger = kermitLogger.withTag("TimeModule")
        val kstZone = ZoneId.of("Asia/Seoul")
        if (BuildConfig.DEBUG) {
            return runCatching {
                val localDateTime = LocalDateTime.parse(BuildConfig.DEBUG_FIXED_DATE)
                val fixedZonedDateTime = localDateTime.atZone(kstZone)

                Clock.fixed(fixedZonedDateTime.toInstant(), kstZone).also {
                    logger.i { "${localDateTime.toLocalDate()} ${localDateTime.toLocalTime()} 로 시간 고정됨" }
                }
            }.getOrElse { e: Throwable ->
                logger.e(e) { "디버그 Clock 생성 실패" }
                Clock.system(kstZone)
            }
        }

        return Clock.system(kstZone)
    }
}
