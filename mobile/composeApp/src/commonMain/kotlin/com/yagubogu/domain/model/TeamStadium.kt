package com.yagubogu.domain.model

val Team.homeStadiumName: String
    get() =
        when (this) {
            Team.HT -> "광주 KIA 챔피언스필드"
            Team.LG -> "잠실야구장"
            Team.OB -> "잠실야구장"
            Team.WO -> "고척스카이돔"
            Team.KT -> "수원 케이티위즈파크"
            Team.SS -> "대구삼성라이온즈파크"
            Team.LT -> "사직야구장"
            Team.SK -> "인천SSG랜더스필드"
            Team.NC -> "창원NC파크"
            Team.HH -> "한화생명이글스파크"
        }
