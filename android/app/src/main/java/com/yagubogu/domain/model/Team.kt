package com.yagubogu.domain.model

enum class Team(
    val nickname: String,
) {
    KIA("타이거즈"),
    LG("트윈스"),
    KIWOOM("히어로즈"),
    KT("위즈"),
    SAMSUNG("라이온즈"),
    LOTTE("자이언츠"),
    SSG("랜더스"),
    NC("다이노스"),
    HANWHA("이글스"),
    DOOSAN("베어스"),
    ;

    companion object {
        fun getById(id: Long): Team = entries[id.toInt() - 1]
    }
}
