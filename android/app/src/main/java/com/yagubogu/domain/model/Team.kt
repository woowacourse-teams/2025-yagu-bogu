package com.yagubogu.domain.model

enum class Team(
    val shortName: String,
    val fullName: String,
) {
    KIA("KIA", "KIA 타이거즈"),
    LG("LG", "LG 트윈스"),
    KIWOOM("키움", "키움 히어로즈"),
    KT("KT", "KT 위즈"),
    SAMSUNG("삼성", "삼성 라이온즈"),
    LOTTE("롯데", "롯데 자이언츠"),
    SSG("SSG", "SSG 랜더스"),
    NC("NC", "NC 다이노스"),
    HANWHA("한화", "한화 이글스"),
    DOOSAN("두산", "두산 베어스"),
    ;

    companion object {
        fun getById(id: Long): Team = entries[id.toInt() - 1]
    }
}
