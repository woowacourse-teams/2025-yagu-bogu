package com.yagubogu.data.dto.request.widget

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WidgetSettingRequest(
    @SerialName("enabled")
    val enabled: Boolean,
)
