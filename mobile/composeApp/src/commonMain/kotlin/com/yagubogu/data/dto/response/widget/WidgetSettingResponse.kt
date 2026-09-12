package com.yagubogu.data.dto.response.widget

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WidgetSettingResponse(
    @SerialName("enabled")
    val enabled: Boolean,
)
