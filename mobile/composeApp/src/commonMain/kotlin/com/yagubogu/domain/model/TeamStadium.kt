package com.yagubogu.domain.model

val Team.homeStadiumId: Long
    get() =
        when (this) {
            Team.HT -> 1L
            Team.LG -> 2L
            Team.OB -> 2L
            Team.WO -> 3L
            Team.KT -> 4L
            Team.SS -> 5L
            Team.LT -> 6L
            Team.SK -> 7L
            Team.NC -> 8L
            Team.HH -> 9L
        }
