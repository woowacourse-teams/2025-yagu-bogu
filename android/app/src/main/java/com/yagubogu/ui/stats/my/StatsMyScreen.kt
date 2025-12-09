package com.yagubogu.ui.stats.my

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.ui.stats.my.component.AttendanceStats
import com.yagubogu.ui.stats.my.component.MyStats
import com.yagubogu.ui.stats.my.component.WinRates
import com.yagubogu.ui.stats.my.model.AverageStats
import com.yagubogu.ui.stats.my.model.StatsMyUiModel
import com.yagubogu.ui.theme.Gray050
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun StatsMyScreen(
    viewModel: StatsMyViewModel,
    reselectFlow: SharedFlow<Unit>,
    modifier: Modifier = Modifier,
) {
    val statsMyUiModel: StatsMyUiModel by viewModel.statsMyUiModel.collectAsStateWithLifecycle()
    val averageStats: AverageStats by viewModel.averageStats.collectAsStateWithLifecycle()
    val scrollState: ScrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.fetchAll()
        reselectFlow.collect {
            scrollState.animateScrollTo(0)
        }
    }

    StatsMyScreen(
        statsMyUiModel = statsMyUiModel,
        averageStats = averageStats,
        scrollState = scrollState,
        modifier = modifier,
    )
}

@Composable
private fun StatsMyScreen(
    statsMyUiModel: StatsMyUiModel,
    averageStats: AverageStats,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
            modifier
                .background(Gray050)
                .verticalScroll(scrollState)
                .padding(20.dp),
    ) {
        WinRates(statsMyUiModel)
        MyStats(statsMyUiModel)
        AttendanceStats(averageStats)
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsMyScreenPreview() {
    StatsMyScreen(statsMyUiModel = StatsMyUiModel(), averageStats = AverageStats())
}
