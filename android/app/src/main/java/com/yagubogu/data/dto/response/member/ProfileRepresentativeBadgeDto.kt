package com.yagubogu.data.dto.response.member

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileRepresentativeBadgeDto(
    @SerialName("imageUrl")
    val imageUrl: String,
)
