package com.yagubogu.ui.place.model

// KBO 10구단 홈구장 고정 목록(실제 백엔드 stadiumId 기준). 전체 구장 목록 API가 없어 프론트에서 보관.
val PLACE_STADIUMS: List<PlaceStadiumItem> =
    listOf(
        PlaceStadiumItem(id = 1L, name = "광주 기아 챔피언스필드"),
        PlaceStadiumItem(id = 2L, name = "잠실 야구장"),
        PlaceStadiumItem(id = 3L, name = "고척 스카이돔"),
        PlaceStadiumItem(id = 4L, name = "수원 KT 위즈파크"),
        PlaceStadiumItem(id = 5L, name = "대구 삼성 라이온즈파크"),
        PlaceStadiumItem(id = 6L, name = "사직야구장"),
        PlaceStadiumItem(id = 7L, name = "인천 SSG 랜더스필드"),
        PlaceStadiumItem(id = 8L, name = "창원 NC 파크"),
        PlaceStadiumItem(id = 9L, name = "대전 한화생명 볼파크"),
    )
