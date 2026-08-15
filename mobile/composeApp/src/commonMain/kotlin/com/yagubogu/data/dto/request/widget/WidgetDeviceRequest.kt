package com.yagubogu.data.dto.request.widget

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WidgetDeviceRequest(
    @SerialName("platform")
    val platform: String,
    @SerialName("deviceId")
    val deviceId: String,
    @SerialName("pushToken")
    val pushToken: String,
    @SerialName("appVersion")
    val appVersion: String? = null,
)
