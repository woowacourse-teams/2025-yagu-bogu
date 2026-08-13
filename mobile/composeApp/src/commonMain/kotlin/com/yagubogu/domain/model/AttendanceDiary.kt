package com.yagubogu.domain.model

data class AttendanceDiary(
    val memo: String,
    val images: List<Image>,
) {
    val hasContent: Boolean = memo.isNotEmpty() || images.isNotEmpty()

    data class Image(
        val id: Long,
        val url: String,
    )
}
