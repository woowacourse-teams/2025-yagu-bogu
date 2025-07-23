package com.yagubogu.domain.model

@JvmInline
value class Latitude(
    val value: Double,
) {
    init {
        require(value in MINIMUM_LATITUDE..MAXIMUM_LATITUDE) { ERROR_INVALID_LATITUDE }
    }

    companion object {
        private const val MINIMUM_LATITUDE = -90.0
        private const val MAXIMUM_LATITUDE = 90.0
        private const val ERROR_INVALID_LATITUDE = "위도는 -90.0 ~ 90.0 사이의 값이어야 합니다."
    }
}
