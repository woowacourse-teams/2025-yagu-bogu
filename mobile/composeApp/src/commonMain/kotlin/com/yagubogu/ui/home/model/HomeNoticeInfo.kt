package com.yagubogu.ui.home.model

data class HomeNoticeInfo(
    override val id: Int,
    val remoteIsShow: Boolean,
    val shouldShowPopup: Boolean,
    override val emoji: String?,
    override val title: String?,
    override val message: String?,
    override val textAlign: String?,
    override val skippableDays: Int?,
) : PopupNoticeInfo
