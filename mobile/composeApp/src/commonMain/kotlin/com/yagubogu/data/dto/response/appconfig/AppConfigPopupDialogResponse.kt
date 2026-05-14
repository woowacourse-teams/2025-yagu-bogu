package com.yagubogu.data.dto.response.appconfig

interface AppConfigPopupDialogResponse {
    val id: Int
    val isShow: Boolean
    val emoji: String?
    val title: String?
    val message: String?
    val textAlign: String?
    val skippableDays: Int?
}
