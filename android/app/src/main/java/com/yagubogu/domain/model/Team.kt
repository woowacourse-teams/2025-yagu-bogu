package com.yagubogu.domain.model

enum class Team(
    val nickname: String,
) {
    HT("타이거즈"),
    LG("트윈스"),
    WO("히어로즈"),
    KT("위즈"),
    SS("라이온즈"),
    LT("자이언츠"),
    SK("랜더스"),
    NC("다이노스"),
    HH("이글스"),
    OB("베어스"),
    ;

    companion object {
        fun getById(id: Long): Team = entries[id.toInt() - 1]
    }
}
