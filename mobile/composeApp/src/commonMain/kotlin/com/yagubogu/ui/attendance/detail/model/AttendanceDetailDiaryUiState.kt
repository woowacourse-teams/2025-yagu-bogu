package com.yagubogu.ui.attendance.detail.model

import com.yagubogu.ui.attendance.detail.AttendanceDetailViewModel.Companion.DIARY_MAX_IMAGE_SIZE
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class AttendanceDetailDiaryUiState(
    val isLoading: Boolean = false,
    val mode: DiaryMode = DiaryMode.WRITE,
    val images: ImmutableList<DiaryImageItem?> = List(DIARY_MAX_IMAGE_SIZE) { null }.toImmutableList(),
    val comment: String = "",
) {
    val isImageEmpty: Boolean = images.all { it == null }
    val emptyImageCount: Long = images.count { it == null }.toLong()

    /** 크기 고정([DIARY_MAX_IMAGE_SIZE]). 빈 슬롯은 null인 리스트, ImagePicker UI에 사용. */
    val imageSlots: ImmutableList<String?> = images.map { it?.uri }.toImmutableList()

    /** 실제 이미지 URI만 포함한 리스트 */
    val imageUris: ImmutableList<String> = images.mapNotNull { it?.uri }.toImmutableList()
}
