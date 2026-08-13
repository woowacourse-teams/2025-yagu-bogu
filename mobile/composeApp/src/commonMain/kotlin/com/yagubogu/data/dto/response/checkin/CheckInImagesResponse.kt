package com.yagubogu.data.dto.response.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckInImagesResponse(
    @SerialName("images")
    val images: List<CheckInImageDto>,
)
