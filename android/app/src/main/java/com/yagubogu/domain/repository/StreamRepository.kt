package com.yagubogu.domain.repository

import com.yagubogu.presentation.home.stadium.SseEvent
import kotlinx.coroutines.flow.Flow

interface StreamRepository {
    suspend fun connect(): Flow<SseEvent>

    fun disconnect()
}
