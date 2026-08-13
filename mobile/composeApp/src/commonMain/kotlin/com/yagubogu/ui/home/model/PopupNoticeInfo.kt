package com.yagubogu.ui.home.model

interface PopupNoticeInfo {
    val id: Int
    val emoji: String?
    val title: String?
    val message: String?
    val textAlign: String?
    val skippableDays: Int?
}
