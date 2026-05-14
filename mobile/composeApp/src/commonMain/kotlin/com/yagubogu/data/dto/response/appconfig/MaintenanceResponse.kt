package com.yagubogu.data.dto.response.appconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MaintenanceResponse(
    @SerialName("is_show")
    override val isShow: Boolean = true, // 점검중 팝업 표시 여부
    @SerialName("id")
    override val id: Int = -1, // 점검 메시지 id
    @SerialName("emoji")
    override val emoji: String? = null, // 다이얼로그 이모지
    @SerialName("title")
    override val title: String? = "인터넷 연결 없음", // 다이얼로그 제목
    @SerialName("message")
    override val message: String? = "야구보구를 사용하기 위해서\n데이터 또는 와이파이 연결이 필요합니다", // 다이얼로그 메시지
    @SerialName("text_align")
    override val textAlign: String? = "Center", // 메시지의 정렬 (Start, End, Justify, Center)
    @SerialName("skippable_days")
    override val skippableDays: Int? = null, // 스킵 가능한 일 수
    @SerialName("is_login_block")
    val isLoginBlock: Boolean = true, // 로그인 차단 여부
) : AppConfigPopupDialogResponse
