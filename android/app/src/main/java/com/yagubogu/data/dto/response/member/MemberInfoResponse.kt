package com.yagubogu.data.dto.response.member

import com.yagubogu.presentation.setting.MemberInfoItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class MemberInfoResponse(
    @SerialName("nickname")
    val nickname: String, // 닉네임
    @SerialName("createdAt")
    val createdAt: String, // 가입일
    @SerialName("favoriteTeam")
    val favoriteTeam: String, // 나의 팀
    @SerialName("profileImageUrl")
    val profileImageUrl: String, // 프로필 이미지
) {
    fun toPresentation(): MemberInfoItem =
        MemberInfoItem(
            nickName = nickname,
            createdAt = LocalDate.parse(createdAt),
            favoriteTeam = favoriteTeam,
            profileImageUrl = profileImageUrl,
        )
}
