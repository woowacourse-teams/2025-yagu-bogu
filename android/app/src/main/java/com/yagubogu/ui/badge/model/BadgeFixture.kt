package com.yagubogu.ui.badge.model

import java.time.LocalDate

val BADGE_ID_0_ACQUIRED_FIXTURE_ =
    BadgeInfoUiModel(
        badge = BadgeUiModel(0, "공포의 주둥아리", "https://i.postimg.cc/jsKmwFjc/5.png", true),
        description = "첫 직관 인증 기념 배지예요! \uD83C\uDF89\n\n이제 당신의 직관 여정이 본격적으로 시작돼요.\n\n앞으로도 다양한 순간들을 기록하며,\n멋진 야구 이야기를 만들어가 보세요!",
        achievedRate = 71,
        achievedAt = LocalDate.of(2025, 9, 22),
        progressRate = 100.0,
    )

val BADGE_ID_0_NOT_ACQUIRED_FIXTURE =
    BadgeInfoUiModel(
        badge = BadgeUiModel(0, "공포의 주둥아리", "https://i.postimg.cc/jsKmwFjc/5.png", false),
        description = "첫 직관 인증 기념 배지예요! \uD83C\uDF89\n\n이제 당신의 직관 여정이 본격적으로 시작돼요.\n\n앞으로도 다양한 순간들을 기록하며,\n멋진 야구 이야기를 만들어가 보세요!",
        achievedRate = 71,
        achievedAt = LocalDate.of(2025, 9, 22),
        progressRate = 80.0,
    )

val BADGE_ID_1_ACQUIRED_FIXTURE =
    BadgeInfoUiModel(
        badge = BadgeUiModel(1, "리드오프", "", true),
        description = "",
        achievedRate = 0,
        achievedAt = LocalDate.of(2025, 9, 22),
        progressRate = 0.0,
    )
