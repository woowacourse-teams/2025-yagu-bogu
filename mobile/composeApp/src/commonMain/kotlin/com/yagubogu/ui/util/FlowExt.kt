package com.yagubogu.ui.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

fun <T> Flow<T>.throttle(
    windowMillis: Long,
    timeSource: TimeSource = TimeSource.Monotonic,
): Flow<T> =
    flow {
        var lastEmittedAt: TimeMark? = null

        collect { value: T ->
            val isWithinWindow: Boolean =
                lastEmittedAt?.let { it.elapsedNow() < windowMillis.milliseconds } == true
            if (isWithinWindow) return@collect

            lastEmittedAt = timeSource.markNow()
            emit(value)
        }
    }
