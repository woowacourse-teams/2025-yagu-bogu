package com.yagubogu.data.dto.request.checkin

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckInRequest(
    @SerialName("stadiumId")
    val stadiumId: Long,
    @SerialName("date")
    val date: LocalDate,
)
