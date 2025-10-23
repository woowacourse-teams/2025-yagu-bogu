package com.yagubogu.data.datasource.stream

import com.yagubogu.data.dto.response.stream.SseCheckInResponse
import kotlinx.coroutines.flow.Flow

interface StreamDataSource {
    fun connect(): Flow<SseCheckInResponse>

    fun disconnect()
}
