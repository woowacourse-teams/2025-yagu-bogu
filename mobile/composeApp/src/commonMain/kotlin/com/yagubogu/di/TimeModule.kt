package com.yagubogu.di

import co.touchlab.kermit.Logger
import com.yagubogu.BuildKonfig
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.Instant

val timeModule =
    module {
        single<Clock> {
            val logger = Logger.withTag("TimeModule")
            val clock: Clock =
                runCatching {
                    if (BuildKonfig.IS_DEBUG) {
                        val kstZone = TimeZone.of("Asia/Seoul")
                        val localDateTime = LocalDateTime.parse(BuildKonfig.DEBUG_FIXED_DATE)
                        val fixedInstant: Instant = localDateTime.toInstant(kstZone)

                        object : Clock {
                            override fun now(): Instant = fixedInstant
                        }
                    } else {
                        Clock.System
                    }
                }.getOrElse { e: Throwable ->
                    logger.e(e) { "디버그 Clock 생성 실패" }
                    Clock.System
                }

            clock
        }
    }
