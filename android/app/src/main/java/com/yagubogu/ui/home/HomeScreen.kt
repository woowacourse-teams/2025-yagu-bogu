package com.yagubogu.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.yagubogu.R
import com.yagubogu.presentation.util.showToast
import com.yagubogu.ui.home.component.CheckInButton
import com.yagubogu.ui.home.component.HomeDialog
import com.yagubogu.ui.home.component.MemberStats
import com.yagubogu.ui.home.component.STADIUM_STATS_UI_MODEL
import com.yagubogu.ui.home.component.StadiumFanRate
import com.yagubogu.ui.home.component.VICTORY_FAIRY_RANKING
import com.yagubogu.ui.home.component.VictoryFairyRanking
import com.yagubogu.ui.home.model.CheckInUiEvent
import com.yagubogu.ui.home.model.MemberStatsUiModel
import com.yagubogu.ui.home.model.StadiumStatsUiModel
import com.yagubogu.ui.home.model.VictoryFairyRanking
import com.yagubogu.ui.theme.Gray050

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val memberStatsUiModel: MemberStatsUiModel by viewModel.memberStatsUiModel.collectAsStateWithLifecycle()
    val stadiumStatsUiModel: StadiumStatsUiModel by viewModel.stadiumStatsUiModel.collectAsStateWithLifecycle()
    val isStadiumStatsExpanded: Boolean by viewModel.isStadiumStatsExpanded.collectAsStateWithLifecycle()
    val victoryFairyRanking: VictoryFairyRanking by viewModel.victoryFairyRanking.collectAsStateWithLifecycle()

    val context: Context = LocalContext.current
    val activity: Activity = LocalActivity.current ?: return

    val locationPermissionManager: LocationPermissionManager =
        remember { LocationPermissionManager(activity) }

    val locationPermissionLauncher: ActivityResultLauncher<Array<String>> =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val isPermissionGranted: Boolean = permissions.any { it.value }
            val shouldShowRationale: Boolean =
                permissions.any { locationPermissionManager.shouldShowRationale(it.key) }
            when {
                isPermissionGranted ->
                    locationPermissionManager.checkLocationSettingsThenAction(viewModel::fetchStadiums)

                // TODO: MainActivity 마이그레이션 시 Snackbar로 대체
                shouldShowRationale -> context.showToast(R.string.home_location_permission_denied_message)

                else -> showPermissionDeniedDialog(context)
            }
        }

    val scrollState: ScrollState = rememberScrollState()
    LaunchedEffect(Unit) {
        viewModel.scrollToTopEvent.collect {
            scrollState.animateScrollTo(0)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.checkInUiEvent.collect { event: CheckInUiEvent ->
            // TODO: MainActivity 마이그레이션 시 Snackbar로 대체
            context.showToast(event.toMessage(context))
        }
    }

    LifecycleStartEffect(viewModel) {
        viewModel.startStreaming()

        onStopOrDispose {
            viewModel.stopStreaming()
        }
    }

    HomeScreen(
        onCheckInClick = {
            checkIn(
                manager = locationPermissionManager,
                launcher = locationPermissionLauncher,
                action = viewModel::fetchStadiums,
            )
        },
        memberStatsUiModel = memberStatsUiModel,
        stadiumStatsUiModel = stadiumStatsUiModel,
        isStadiumStatsExpanded = isStadiumStatsExpanded,
        onStadiumStatsClick = viewModel::toggleStadiumStats,
        onStadiumStatsRefresh = viewModel::refreshStadiumStats,
        victoryFairyRanking = victoryFairyRanking,
        onVictoryFairyRankingClick = viewModel::fetchMemberProfile,
        modifier = modifier,
        scrollState = scrollState,
    )
    HomeDialog(viewModel)
}

@Composable
private fun HomeScreen(
    onCheckInClick: () -> Unit,
    memberStatsUiModel: MemberStatsUiModel,
    stadiumStatsUiModel: StadiumStatsUiModel,
    isStadiumStatsExpanded: Boolean,
    onStadiumStatsClick: () -> Unit,
    onStadiumStatsRefresh: () -> Unit,
    victoryFairyRanking: VictoryFairyRanking,
    onVictoryFairyRankingClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050)
                .verticalScroll(scrollState)
                .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        CheckInButton(
            onClick = {
                onCheckInClick()
                Firebase.analytics.logEvent("check_in", null)
            },
            modifier = Modifier.fillMaxWidth(),
        )
        MemberStats(uiModel = memberStatsUiModel)

        if (stadiumStatsUiModel.stadiumFanRates.isNotEmpty()) {
            StadiumFanRate(
                uiModel = stadiumStatsUiModel,
                isExpanded = isStadiumStatsExpanded,
                onClick = onStadiumStatsClick,
                onRefresh = onStadiumStatsRefresh,
            )
        }
        VictoryFairyRanking(
            ranking = victoryFairyRanking,
            onRankingItemClick = onVictoryFairyRankingClick,
        )
    }
}

private fun checkIn(
    manager: LocationPermissionManager,
    launcher: ActivityResultLauncher<Array<String>>,
    action: () -> Unit,
) {
    if (manager.isPermissionGranted()) {
        manager.checkLocationSettingsThenAction(action)
    } else {
        manager.requestPermissions(launcher)
    }
}

private fun showPermissionDeniedDialog(context: Context) {
    AlertDialog
        .Builder(context)
        .setTitle(R.string.permission_dialog_location_title)
        .setMessage(R.string.permission_dialog_location_description)
        .setPositiveButton(R.string.permission_dialog_open_settings) { _, _ ->
            openAppSettings(context)
        }.setNegativeButton(R.string.all_cancel, null)
        .setCancelable(false)
        .show()
}

private fun openAppSettings(context: Context) {
    val intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    context.startActivity(intent)
}

private fun CheckInUiEvent.toMessage(context: Context): String =
    when (this) {
        is CheckInUiEvent.Success ->
            context.getString(R.string.home_check_in_success_message, stadium.name)

        CheckInUiEvent.NoGame ->
            context.getString(R.string.home_check_in_no_game_message)

        CheckInUiEvent.OutOfRange ->
            context.getString(R.string.home_check_in_out_of_range_message)

        CheckInUiEvent.AlreadyCheckedIn ->
            context.getString(R.string.home_already_checked_in_message)

        CheckInUiEvent.LocationFetchFailed ->
            context.getString(R.string.home_check_in_location_fetch_failed_message)

        CheckInUiEvent.NetworkFailed ->
            context.getString(R.string.home_check_in_network_failed_message)
    }

@Preview
@Composable
private fun HomeScreenPreview() {
    HomeScreen(
        onCheckInClick = {},
        memberStatsUiModel =
            MemberStatsUiModel(
                myTeam = "KIA",
                attendanceCount = 24,
                winRate = 75,
            ),
        stadiumStatsUiModel = STADIUM_STATS_UI_MODEL,
        isStadiumStatsExpanded = false,
        onStadiumStatsClick = {},
        onStadiumStatsRefresh = {},
        victoryFairyRanking = VICTORY_FAIRY_RANKING,
        onVictoryFairyRankingClick = {},
    )
}
