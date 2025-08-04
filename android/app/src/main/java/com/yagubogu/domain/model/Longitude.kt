package com.yagubogu.domain.model

@JvmInline
value class Longitude(
    val value: Double,
) {
    init {
        require(value in MINIMUM_LONGITUDE..MAXIMUM_LONGITUDE) { ERROR_INVALID_LONGITUDE }
    }

    companion object {
        private const val MINIMUM_LONGITUDE = -180.0
        private const val MAXIMUM_LONGITUDE = 180.0
        private const val ERROR_INVALID_LONGITUDE = "경도는 -180.0 ~ 180.0 사이의 값이어야 합니다."
    }
}
