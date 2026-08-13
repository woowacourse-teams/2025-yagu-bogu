package com.yagubogu.ui.stats.my.component

import androidx.compose.runtime.Immutable

@Immutable
sealed interface StatItemValue {
    data object Loading : StatItemValue

    data object NoData : StatItemValue

    data class Data(
        val text: String,
    ) : StatItemValue
}
