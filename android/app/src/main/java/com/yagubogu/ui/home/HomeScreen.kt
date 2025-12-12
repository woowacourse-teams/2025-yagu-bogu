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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.yagubogu.R
import com.yagubogu.presentation.home.HomeViewModel
import com.yagubogu.presentation.home.LocationPermissionManager
import com.yagubogu.presentation.home.model.MemberStatsUiModel
import com.yagubogu.presentation.home.model.StadiumStatsUiModel
import com.yagubogu.presentation.home.ranking.VictoryFairyRanking
import com.yagubogu.ui.home.component.CheckInButton
import com.yagubogu.ui.home.component.HomeDialog
import com.yagubogu.ui.home.component.MemberStats
import com.yagubogu.ui.home.component.STADIUM_STATS_UI_MODEL
import com.yagubogu.ui.home.component.StadiumFanRate
import com.yagubogu.ui.home.component.VICTORY_FAIRY_RANKING
import com.yagubogu.ui.home.component.VictoryFairyRanking
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

                shouldShowRationale -> {
//                    binding.root.showSnackbar(
//                        R.string.home_location_permission_denied_message,
//                        R.id.bnv_navigation,
//                    )
                }

                else -> showPermissionDeniedDialog(context)
            }
        }

    HomeScreen(
        onCheckInClick = {
            checkIn(
                locationPermissionManager,
                locationPermissionLauncher,
                viewModel::fetchStadiums,
            )
        },
        memberStatsUiModel = memberStatsUiModel,
        stadiumStatsUiModel = stadiumStatsUiModel,
        isStadiumStatsExpanded = isStadiumStatsExpanded,
        onStadiumStatsClick = viewModel::toggleStadiumStats,
        onStadiumStatsRefresh = viewModel::refreshStadiumStats,
        victoryFairyRanking = victoryFairyRanking,
        onVictoryFairyRankingClick = viewModel::fetchMemberProfile,
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
) {
    val scrollState: ScrollState = rememberScrollState()

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
