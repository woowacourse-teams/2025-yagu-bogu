package com.yagubogu.ui.stats

import org.jetbrains.compose.resources.StringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.stats_tab_my_stats
import yagubogu.composeapp.generated.resources.stats_tab_stats_detail

enum class StatsTab(
    val titleRes: StringResource,
) {
    MY_STATS(Res.string.stats_tab_my_stats),
    DETAIL_STATS(Res.string.stats_tab_stats_detail),
}
