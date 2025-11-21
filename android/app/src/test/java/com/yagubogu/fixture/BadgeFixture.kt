package com.yagubogu.fixture

import com.yagubogu.data.dto.response.member.BadgeDto
import com.yagubogu.data.dto.response.member.RepresentativeBadgeDto
import kotlinx.datetime.LocalDateTime

val REPRESENTATIVE_BADGE_FIXTURE =
    RepresentativeBadgeDto(
        id = 0,
        name = "공포의 주둥아리",
        badgeImageUrl = "https://i.postimg.cc/jsKmwFjc/5.png",
    )

val BADGE_ID_0_ACQUIRED_FIXTURE =
    BadgeDto(
        id = 0,
        name = "공포의 주둥아리",
        description = "첫 직관 인증 기념 배지예요! \uD83C\uDF89\n\n이제 당신의 직관 여정이 본격적으로 시작돼요.\n\n앞으로도 다양한 순간들을 기록하며,\n멋진 야구 이야기를 만들어가 보세요!",
        acquired = true,
        achievedAt = LocalDateTime(2025, 9, 22, 0, 0),
        badgeImageUrl = "https://i.postimg.cc/jsKmwFjc/5.png",
        progressRate = 100.0,
        achievedRate = 71.0,
    )

val BADGE_ID_1_ACQUIRED_FIXTURE =
    BadgeDto(
        id = 1,
        name = "리드오프",
        description = "",
        acquired = true,
        achievedAt = LocalDateTime(2025, 9, 22, 0, 0),
        badgeImageUrl = "",
        progressRate = 0.0,
        achievedRate = 0.0,
    )
