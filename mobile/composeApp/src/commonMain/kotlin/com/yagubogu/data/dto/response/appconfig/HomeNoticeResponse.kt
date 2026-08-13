package com.yagubogu.data.dto.response.appconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeNoticeResponse(
    @SerialName("is_show")
    override val isShow: Boolean = false, // 홈화면 공지사항 팝업 표시 여부
    @SerialName("id")
    override val id: Int = -1, // 홈화면 메시지 id
    @SerialName("emoji")
    override val emoji: String? = null, // 다이얼로그 이모지
    @SerialName("title")
    override val title: String? = null, // 다이얼로그 제목
    @SerialName("message")
    override val message: String? = null, // 다이얼로그 메시지
    @SerialName("text_align")
    override val textAlign: String? = "Center", // 메시지의 정렬 (Start, End, Justify, Center)
    @SerialName("skippable_days")
    override val skippableDays: Int? = null, // 스킵 가능한 일 수
) : AppConfigPopupDialogResponse
