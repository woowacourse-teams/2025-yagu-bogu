package com.yagubogu.domain.model

val Team.homeStadiumName: String
    get() =
        when (this) {
            Team.HT -> "광주 기아 챔피언스필드"
            Team.LG -> "잠실 야구장"
            Team.OB -> "잠실 야구장"
            Team.WO -> "고척 스카이돔"
            Team.KT -> "수원 KT 위즈파크"
            Team.SS -> "대구 삼성 라이온즈파크"
            Team.LT -> "사직야구장"
            Team.SK -> "인천 SSG 랜더스필드"
            Team.NC -> "창원 NC 파크"
            Team.HH -> "대전 한화생명 볼파크"
        }
