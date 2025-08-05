package com.yagubogu.domain.model

@JvmInline
value class Distance(
    val value: Double,
) {
    init {
        require(value >= MINIMUM_DISTANCE) { ERROR_INVALID_DISTANCE }
    }

    fun isWithin(distance: Distance): Boolean = value <= distance.value

    companion object {
        private const val MINIMUM_DISTANCE = 0.0
        private const val ERROR_INVALID_DISTANCE = "거리는 0.0 이상이어야 합니다."
    }
}
