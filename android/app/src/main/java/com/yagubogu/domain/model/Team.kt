package com.yagubogu.domain.model

import androidx.annotation.ColorRes
import com.yagubogu.R

enum class Team(
    val shortName: String,
    val fullName: String,
    @ColorRes
    val color: Int,
) {
    KIA("KIA", "KIA 타이거즈", R.color.team_kia),
    LG("LG", "LG 트윈스", R.color.team_lg),
    KIWOOM("키움", "키움 히어로즈", R.color.team_kiwoom),
    KT("KT", "KT 위즈", R.color.team_kt),
    SAMSUNG("삼성", "삼성 라이온즈", R.color.team_samsung),
    LOTTE("롯데", "롯데 자이언츠", R.color.team_lotte),
    SSG("SSG", "SSG 랜더스", R.color.team_ssg),
    NC("NC", "NC 다이노스", R.color.team_nc),
    HANWHA("한화", "한화 이글스", R.color.team_hanwha),
    DOOSAN("두산", "두산 베어스", R.color.team_doosan),
    ;

    companion object {
        fun getById(id: Int): Team? = if (id in 1..entries.size) entries[id - 1] else null
    }
}
