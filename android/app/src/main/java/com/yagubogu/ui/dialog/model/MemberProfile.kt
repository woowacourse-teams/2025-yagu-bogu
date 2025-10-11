package com.yagubogu.ui.dialog.model

data class MemberProfile(
    val nickname: String, // 회원 닉네임
    val enterDate: String, // 회원 가입일 (YYYY-MM-DD)
    val profileImageUrl: String, // 프로필 이미지 주소
    val favoriteTeam: String, // 응원하는 팀
    val representativeBadgeName: String, // 대표 배지 이름
    val representativeBadgeImageUrl: String, // 대표 배지 이미지 주소
    val victoryFairyRanking: Int, // 승리 요정 랭킹
    val victoryFairyScore: Int, // 승리 요정 점수
    val checkInCounts: Int, // 누적 직관 횟수
    val checkInWinRate: String, // 직관 승률
)
