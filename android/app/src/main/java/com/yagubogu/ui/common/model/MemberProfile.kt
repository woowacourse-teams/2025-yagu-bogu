package com.yagubogu.ui.common.model

import java.time.LocalDate

data class MemberProfile(
    val nickname: String, // 회원 닉네임
    val enterDate: LocalDate, // 회원 가입일 (YYYY-MM-DD)
    val profileImageUrl: String, // 프로필 이미지 주소
    val favoriteTeam: String, // 응원팀
    val representativeBadgeImageUrl: String?, // 대표 배지 이미지 주소
    val victoryFairyRanking: Long?, // 승리 요정 랭킹
    val victoryFairyScore: Double?, // 승리 요정 점수
    val victoryFairyRankingWithinTeam: Long?, // 팀 별 승리 요정 랭킹
    val checkInCounts: Int?, // 누적 직관 횟수
    val checkInWinRate: Double?, // 직관 승률
    val winCounts: Int?, // 직관 승리 횟수
    val drawCounts: Int?, // 직관 무승부 횟수
    val loseCounts: Int?, // 직관 패배 횟수
    val recentCheckInDate: LocalDate?, // 최근 직관 날짜
) {
    val winDrawLose: String = "${winCounts ?: "-"}/${drawCounts ?: "-"}/${loseCounts ?: "-"}"
}

val MEMBER_PROFILE_FIXTURE =
    MemberProfile(
        nickname = "Jake Wharton",
        enterDate = LocalDate.of(2025, 10, 1),
        profileImageUrl = "https://avatars.githubusercontent.com/u/66577?v=4",
        favoriteTeam = "KIA",
        representativeBadgeImageUrl = "",
        victoryFairyRanking = 275,
        victoryFairyScore = 33.1,
        victoryFairyRankingWithinTeam = 154,
        checkInCounts = 11,
        checkInWinRate = 60.0,
        winCounts = 10,
        drawCounts = 3,
        loseCounts = 5,
        recentCheckInDate = LocalDate.of(2025, 10, 19),
    )

val MEMBER_PROFILE_FIXTURE_NULL =
    MemberProfile(
        nickname = "Alvaro Bruce",
        enterDate = LocalDate.of(2025, 10, 1),
        profileImageUrl = "https://www.google.com/#q=quod",
        favoriteTeam = "삼성",
        representativeBadgeImageUrl = null,
        victoryFairyRanking = null,
        victoryFairyScore = null,
        victoryFairyRankingWithinTeam = null,
        checkInCounts = null,
        checkInWinRate = 0.0,
        winCounts = null,
        drawCounts = null,
        loseCounts = null,
        recentCheckInDate = null,
    )
