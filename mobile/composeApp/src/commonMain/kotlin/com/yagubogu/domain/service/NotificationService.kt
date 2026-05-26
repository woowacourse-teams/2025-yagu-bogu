package com.yagubogu.domain.service

interface NotificationService {
    suspend fun sendStadiumEntryNotification(
        stadiumName: String,
        stadiumId: Int,
    )
}
