package com.yagubogu.data.dto.response.member

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BadgeResponse(
    @SerialName("representativeBadge")
    val representativeBadge: RepresentativeBadgeDto,
    @SerialName("badges")
    val badges: List<BadgeDto>,
)
