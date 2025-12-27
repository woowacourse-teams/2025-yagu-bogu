package com.yagubogu.presentation.mapper

import com.yagubogu.data.dto.response.member.BadgeDto
import com.yagubogu.data.dto.response.member.MemberInfoResponse
import com.yagubogu.data.dto.response.member.MemberProfileResponse
import com.yagubogu.data.dto.response.member.RepresentativeBadgeDto
import com.yagubogu.data.dto.response.presigned.PresignedUrlCompleteResponse
import com.yagubogu.data.dto.response.presigned.PresignedUrlStartResponse
import com.yagubogu.ui.badge.model.BadgeInfoUiModel
import com.yagubogu.ui.badge.model.BadgeUiModel
import com.yagubogu.ui.common.model.MemberProfile
import com.yagubogu.ui.setting.component.model.MemberInfoItem
import com.yagubogu.ui.setting.component.model.PresignedUrlCompleteItem
import com.yagubogu.ui.setting.component.model.PresignedUrlItem
import kotlinx.datetime.toJavaLocalDate
import java.time.LocalDate

fun MemberInfoResponse.toUiModel(): MemberInfoItem =
    MemberInfoItem(
        nickName = nickname,
        createdAt = LocalDate.parse(createdAt),
        favoriteTeam = favoriteTeam,
        profileImageUrl = profileImageUrl,
    )

fun RepresentativeBadgeDto.toUiModel(): BadgeUiModel =
    BadgeUiModel(
        id = id,
        imageUrl = badgeImageUrl,
        name = name,
        isAcquired = true,
    )

fun BadgeDto.toUiModel(): BadgeInfoUiModel {
    val badge =
        BadgeUiModel(
            id = id,
            imageUrl = badgeImageUrl,
            name = name,
            isAcquired = acquired,
        )

    return BadgeInfoUiModel(
        badge = badge,
        description = description,
        achievedRate = achievedRate.toInt(),
        achievedAt = achievedAt?.date?.toJavaLocalDate(),
        progressRate = progressRate,
    )
}

fun PresignedUrlStartResponse.toUiModel(): PresignedUrlItem =
    PresignedUrlItem(
        key = key,
        url = url,
    )

fun PresignedUrlCompleteResponse.toUiModel(): PresignedUrlCompleteItem =
    PresignedUrlCompleteItem(
        imageUrl = url,
    )

fun MemberProfileResponse.toUiModel(): MemberProfile =
    MemberProfile(
        nickname = nickname,
        enterDate = enterDate.toJavaLocalDate(),
        profileImageUrl = profileImageUrl,
        favoriteTeam = favoriteTeam,
        representativeBadgeImageUrl = representativeBadge?.imageUrl,
        victoryFairyRanking = victoryFairyProfile.ranking,
        victoryFairyScore = victoryFairyProfile.score,
        victoryFairyRankingWithinTeam = victoryFairyProfile.rankWithinTeam,
        checkInCounts = checkIn.counts,
        checkInWinRate = checkIn.winRate,
        winCounts = checkIn.winCounts,
        drawCounts = checkIn.drawCounts,
        loseCounts = checkIn.loseCounts,
        recentCheckInDate = checkIn.recentCheckInDate?.toJavaLocalDate(),
    )
