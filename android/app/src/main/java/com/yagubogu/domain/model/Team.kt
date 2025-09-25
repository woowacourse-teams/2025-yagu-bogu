package com.yagubogu.domain.model

enum class Team(
    val shortname: String,
) {
    NOT_SELECTED("없음"),
    HT("KIA"),
    LG("LG"),
    WO("키움"),
    KT("KT"),
    SS("삼성"),
    LT("롯데"),
    SK("SSG"),
    NC("NC"),
    HH("한화"),
    OB("두산"),
    ;

    companion object {
        fun getByCode(code: String): Team = entries.find { it.name == code } ?: throw IllegalArgumentException()
    }
}
