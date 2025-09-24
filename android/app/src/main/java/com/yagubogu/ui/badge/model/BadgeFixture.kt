package com.yagubogu.ui.badge.model

import java.time.LocalDate

val BADGE_ACQUIRED_FIXTURE =
    BadgeUiModel(
        imageUrl = "https://i.postimg.cc/jsKmwFjc/5.png",
        name = "공포의 주둥아리",
        description = "첫 직관 인증 기념 배지예요! \uD83C\uDF89\n\n이제 당신의 직관 여정이 본격적으로 시작돼요.\n\n앞으로도 다양한 순간들을 기록하며,\n멋진 야구 이야기를 만들어가 보세요!",
        isAcquired = true,
        achievedRate = 71,
        achievedAt = LocalDate.of(2025, 9, 22),
        progressRate = 100.0,
    )

val BADGE_NOT_ACQUIRED_FIXTURE =
    BadgeUiModel(
        imageUrl = "https://i.postimg.cc/jsKmwFjc/5.png",
        name = "공포의 주둥아리",
        description = "첫 직관 인증 기념 배지예요! \uD83C\uDF89\n\n이제 당신의 직관 여정이 본격적으로 시작돼요.\n\n앞으로도 다양한 순간들을 기록하며,\n멋진 야구 이야기를 만들어가 보세요!",
        isAcquired = false,
        achievedRate = 71,
        achievedAt = LocalDate.of(2025, 9, 22),
        progressRate = 80.0,
    )
