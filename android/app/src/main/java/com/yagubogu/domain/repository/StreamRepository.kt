package com.yagubogu.domain.repository

import com.yagubogu.data.dto.response.stream.SseCheckInResponse
import kotlinx.coroutines.flow.Flow

interface StreamRepository {
    fun connect(): Flow<SseCheckInResponse>

    fun disconnect()
}
