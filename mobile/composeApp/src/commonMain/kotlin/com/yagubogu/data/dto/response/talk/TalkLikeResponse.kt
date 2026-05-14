package com.yagubogu.data.dto.response.talk

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TalkLikeResponse(
    @SerialName("liked")
    val liked: Boolean,
    @SerialName("likeCount")
    val likeCount: Int,
)
