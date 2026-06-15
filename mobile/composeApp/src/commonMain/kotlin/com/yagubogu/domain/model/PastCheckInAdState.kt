package com.yagubogu.domain.model

class PastCheckInAdState(
    private val isEnabled: Boolean,
    private val startCount: Int,
    private val interval: Int,
) {
    private var count = 0

    fun incrementAndShouldShowAd(): Boolean {
        count++
        if (!isEnabled || interval <= 0 || count < startCount) return false
        return (count - startCount) % interval == 0
    }
}
