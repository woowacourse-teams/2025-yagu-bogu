package com.yagubogu.ui.home.model

data class MaintenanceInfo(
    override val id: Int,
    val remoteIsShow: Boolean,
    val shouldShowPopup: Boolean,
    override val emoji: String?,
    override val title: String?,
    override val message: String?,
    override val textAlign: String?,
    override val skippableDays: Int?,
    val isLoginBlock: Boolean,
) : PopupNoticeInfo
