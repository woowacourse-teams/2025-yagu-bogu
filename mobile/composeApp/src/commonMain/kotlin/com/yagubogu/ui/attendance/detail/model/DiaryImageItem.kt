package com.yagubogu.ui.attendance.detail.model

data class DiaryImageItem(
    val id: Long? = null, // 로컬에만 저장된 경우 id = null
    val uri: String? = null, // 로컬 URI 또는 서버 URL
)
