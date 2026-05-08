package com.yagubogu.ui.home

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yagubogu.analytics.AnalyticsLogger
import com.yagubogu.ui.common.AdUnitIds
import com.yagubogu.ui.common.component.BannerAd
import com.yagubogu.ui.common.component.BannerAdType
import com.yagubogu.ui.home.component.CHECK_IN_RANKING
import com.yagubogu.ui.home.component.CheckInButton
import com.yagubogu.ui.home.component.MemberStats
import com.yagubogu.ui.home.component.OpeningCountdown
import com.yagubogu.ui.home.component.STADIUM_STATS_UI_MODEL
import com.yagubogu.ui.home.component.StadiumFanRate
import com.yagubogu.ui.home.component.VICTORY_FAIRY_RANKING
import com.yagubogu.ui.home.component.dialog.HomeDialog
import com.yagubogu.ui.home.component.dialog.PermissionDeniedDialog
import com.yagubogu.ui.home.model.CheckInUiEvent
import com.yagubogu.ui.home.model.LocationPermissionManager
import com.yagubogu.ui.home.model.MemberStatsUiModel
import com.yagubogu.ui.home.model.PermissionState
import com.yagubogu.ui.home.model.StadiumStatsUiModel
import com.yagubogu.ui.ranking.component.Ranking
import com.yagubogu.ui.ranking.model.RankingType
import com.yagubogu.ui.ranking.model.RankingUiModel
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.util.BackPressHandler
import com.yagubogu.ui.util.LocalSnackbarHostState
import com.yagubogu.ui.util.LocalSnackbarScope
import com.yagubogu.ui.util.showSingleSnackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.home_already_checked_in_message
import yagubogu.composeapp.generated.resources.home_check_in_location_fetch_failed_message
import yagubogu.composeapp.generated.resources.home_check_in_network_failed_message
import yagubogu.composeapp.generated.resources.home_check_in_no_game_message
import yagubogu.composeapp.generated.resources.home_check_in_out_of_range_message
import yagubogu.composeapp.generated.resources.home_check_in_success_message
import yagubogu.composeapp.generated.resources.home_location_permission_denied_message
import yagubogu.composeapp.generated.resources.home_location_settings_disabled

@Composable
expect fun rememberLocationPermissionManager(onPermissionResult: (Map<String, Boolean>) -> Unit): LocationPermissionManager

@Composable
expect fun rememberAppSettingsOpener(): () -> Unit

@Composable
fun HomeScreen(
    scrollToTopEvent: SharedFlow<Unit>,
    onLoading: (Boolean) -> Unit,
    onRankingShowMoreClick: (RankingType) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val memberStatsUiModel: MemberStatsUiModel by viewModel.memberStatsUiModel.collectAsStateWithLifecycle()
    val stadiumStatsUiModel: StadiumStatsUiModel by viewModel.stadiumStatsUiModel.collectAsStateWithLifecycle()
    val isStadiumStatsExpanded: Boolean by viewModel.isStadiumStatsExpanded.collectAsStateWithLifecycle()
    val checkInRanking: RankingUiModel by viewModel.checkInRanking.collectAsStateWithLifecycle()
    val victoryFairyRanking: RankingUiModel by viewModel.victoryFairyRanking.collectAsStateWithLifecycle()
    val leftSecondsUntilOpening: StateFlow<Long> = viewModel.leftSecondsUntilOpening
    val openingHour: Int = viewModel.openingHour

    var permissionState: PermissionState by remember { mutableStateOf(PermissionState.IDLE) }

    val snackbarScope: CoroutineScope = LocalSnackbarScope.current
    val snackbarHostState = LocalSnackbarHostState.current

    val locationSettingsDisabledMessage: String =
        stringResource(Res.string.home_location_settings_disabled)
    val locationPermissionDeniedMessage: String =
        stringResource(Res.string.home_location_permission_denied_message)

    val openAppSettings: () -> Unit = rememberAppSettingsOpener()

    val locationPermissionManager =
        rememberLocationPermissionManager(
            onPermissionResult = { permissions: Map<String, Boolean> ->
                val isPermissionGranted: Boolean = permissions.any { it.value }
                permissionState =
                    if (isPermissionGranted) {
                        PermissionState.GRANTED
                    } else {
                        PermissionState.DENIED
                    }
            },
        )

    LaunchedEffect(Unit) {
        viewModel.fetchAll()

        launch {
            viewModel.checkInUiEvent.collect { event: CheckInUiEvent ->
                snackbarHostState.showSnackbar(event.toMessage())
            }
        }

        launch {
            viewModel.isCheckInLoading.collect { isLoading: Boolean ->
                onLoading(isLoading)
            }
        }
    }

    LaunchedEffect(permissionState) {
        if (permissionState == PermissionState.GRANTED) {
            locationPermissionManager.checkLocationSettingsThenAction(
                onSuccess = viewModel::fetchStadiums,
                onSettingsDisabled = {
                    snackbarHostState.showSingleSnackbar(
                        scope = snackbarScope,
                        message = locationSettingsDisabledMessage,
                    )
                },
            )
            permissionState = PermissionState.IDLE
        }
    }

    BackPressHandler()

    HomeScreen(
        onCheckInClick = {
            when {
                locationPermissionManager.isPermissionGranted() -> {
                    locationPermissionManager.checkLocationSettingsThenAction(
                        onSuccess = viewModel::fetchStadiums,
                        onSettingsDisabled = {
                            snackbarHostState.showSingleSnackbar(
                                scope = snackbarScope,
                                message = locationSettingsDisabledMessage,
                            )
                        },
                    )
                }

                locationPermissionManager.shouldShowRationale() -> {
                    snackbarScope.launch {
                        snackbarHostState.showSingleSnackbar(
                            scope = snackbarScope,
                            message = locationPermissionDeniedMessage,
                        )
                    }
                }

                else -> locationPermissionManager.requestPermissions()
            }
        },
        memberStatsUiModel = memberStatsUiModel,
        stadiumStatsUiModel = stadiumStatsUiModel,
        isStadiumStatsExpanded = isStadiumStatsExpanded,
        onStadiumStatsClick = viewModel::toggleStadiumStats,
        onStadiumStatsRefresh = viewModel::refreshStadiumStats,
        checkInRanking = checkInRanking,
        victoryFairyRanking = victoryFairyRanking,
        onRankingShowMoreClick = onRankingShowMoreClick,
        onMemberProfileClick = viewModel::fetchMemberProfile,
        leftSecondsUntilOpening = leftSecondsUntilOpening,
        openingHour = openingHour,
        modifier = modifier,
        scrollToTopEvent = scrollToTopEvent,
    )

    HomeDialog(viewModel)

    if (permissionState == PermissionState.DENIED) {
        PermissionDeniedDialog(
            onOpenSettings = {
                permissionState = PermissionState.IDLE
                openAppSettings()
            },
            onDismiss = {
                permissionState = PermissionState.IDLE
            },
        )
    }
}

@Composable
private fun HomeScreen(
    onCheckInClick: () -> Unit,
    memberStatsUiModel: MemberStatsUiModel,
    stadiumStatsUiModel: StadiumStatsUiModel,
    isStadiumStatsExpanded: Boolean,
    onStadiumStatsClick: () -> Unit,
    onStadiumStatsRefresh: () -> Unit,
    checkInRanking: RankingUiModel,
    victoryFairyRanking: RankingUiModel,
    onRankingShowMoreClick: (RankingType) -> Unit,
    onMemberProfileClick: (Long) -> Unit,
    leftSecondsUntilOpening: StateFlow<Long>,
    openingHour: Int,
    modifier: Modifier = Modifier,
    scrollToTopEvent: SharedFlow<Unit> = MutableSharedFlow(),
) {
    val scrollState: ScrollState = rememberScrollState()
    val leftSeconds: Long by leftSecondsUntilOpening.collectAsStateWithLifecycle()
    var isCountdownVisible: Boolean by remember { mutableStateOf(false) }

    if (!isCountdownVisible && leftSeconds > 0) {
        isCountdownVisible = true
    }

    LaunchedEffect(Unit) {
        scrollToTopEvent.collect {
            scrollState.animateScrollTo(0)
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        CheckInButton(
            onClick = {
                onCheckInClick()
                AnalyticsLogger.logEvent("check_in")
            },
            modifier = Modifier.fillMaxWidth(),
        )
        MemberStats(uiModel = memberStatsUiModel)

        if (isCountdownVisible) {
            OpeningCountdown(
                leftSecondsFlow = leftSecondsUntilOpening,
                openingHour = openingHour,
            )
        }

        if (stadiumStatsUiModel.stadiumFanRates.isNotEmpty()) {
            StadiumFanRate(
                uiModel = stadiumStatsUiModel,
                isExpanded = isStadiumStatsExpanded,
                onClick = onStadiumStatsClick,
                onRefresh = onStadiumStatsRefresh,
            )
        }

        BannerAd(
            adUnitId = AdUnitIds.homeBanner,
            bannerAdType = BannerAdType.BANNER,
        )

        if (checkInRanking.topRankings.isNotEmpty()) {
            Ranking(
                ranking = checkInRanking,
                onRankingShowMoreClick = onRankingShowMoreClick,
                onMemberProfileClick = onMemberProfileClick,
            )
        }
        if (victoryFairyRanking.topRankings.isNotEmpty()) {
            Ranking(
                ranking = victoryFairyRanking,
                onRankingShowMoreClick = onRankingShowMoreClick,
                onMemberProfileClick = onMemberProfileClick,
            )
        }
    }
}

private suspend fun CheckInUiEvent.toMessage(): String =
    when (this) {
        is CheckInUiEvent.Success ->
            getString(Res.string.home_check_in_success_message, stadium.name)

        CheckInUiEvent.NoGame ->
            getString(Res.string.home_check_in_no_game_message)

        CheckInUiEvent.OutOfRange ->
            getString(Res.string.home_check_in_out_of_range_message)

        CheckInUiEvent.AlreadyCheckedIn ->
            getString(Res.string.home_already_checked_in_message)

        CheckInUiEvent.LocationFetchFailed ->
            getString(Res.string.home_check_in_location_fetch_failed_message)

        CheckInUiEvent.NetworkFailed ->
            getString(Res.string.home_check_in_network_failed_message)
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
        checkInRanking = CHECK_IN_RANKING,
        victoryFairyRanking = VICTORY_FAIRY_RANKING,
        onRankingShowMoreClick = {},
        onMemberProfileClick = {},
        leftSecondsUntilOpening = MutableStateFlow(1_000_000L),
        openingHour = 14,
    )
}
