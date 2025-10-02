package com.yagubogu.domain.repository

import com.yagubogu.presentation.home.model.CheckInSseEvent
import kotlinx.coroutines.flow.Flow

interface StreamRepository {
    suspend fun connect(): Flow<CheckInSseEvent>

    fun disconnect()
}
