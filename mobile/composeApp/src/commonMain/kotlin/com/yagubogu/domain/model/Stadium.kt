package com.yagubogu.domain.model

data class Stadium(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
) {
    companion object {
        val ALL_LIST =
            listOf(
                Stadium(1, "광주 기아 챔피언스필드", 35.168139, 126.889111),
                Stadium(2, "잠실 야구장", 37.512150, 127.071976),
                Stadium(3, "고척 스카이돔", 37.498222, 126.867250),
                Stadium(4, "수원 KT 위즈파크", 37.299759, 127.009781),
                Stadium(5, "대구 삼성 라이온즈파크", 35.841111, 128.681667),
                Stadium(6, "사직야구장", 35.194077, 129.061584),
                Stadium(7, "인천 SSG 랜더스필드", 37.436778, 126.693306),
                Stadium(8, "창원 NC 파크", 35.222754, 128.582251),
                Stadium(9, "대전 한화생명 볼파크", 36.316589, 127.431211),
                Stadium(10, "울산 문수 야구장", 35.532334, 129.265575),
                Stadium(11, "월명종합경기장 야구장", 35.966360, 126.748161),
                Stadium(12, "청주 야구장", 36.638840, 127.470149),
                Stadium(13, "포항 야구장", 36.008273, 129.359410),
                Stadium(14, "한화생명 이글스파크", 36.317178, 127.429167),
                Stadium(15, "대구시민운동장 야구장", 35.881162, 128.586371),
                Stadium(16, "무등 야구장", 35.169165, 126.887245),
                Stadium(17, "마산 야구장", 35.220855, 128.581050),
                Stadium(18, "숭의 야구장", 37.466591, 126.643239),
                Stadium(19, "삼성 라이온즈 볼파크", 35.864844, 128.805667),
            )

        fun getStadiumById(id: Int): Stadium? = ALL_LIST.find { it.id == id }

        const val GEOFENCE_RADIUS_METERS = 600f
    }
}
