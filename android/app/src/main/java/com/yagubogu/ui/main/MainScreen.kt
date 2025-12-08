package com.yagubogu.ui.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.yagubogu.R
import com.yagubogu.ui.main.component.MainNavigationBar
import com.yagubogu.ui.main.component.MainToolbar
import com.yagubogu.ui.stats.StatsScreen
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardSemiBold12
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.NavigationState
import com.yagubogu.ui.util.Navigator
import com.yagubogu.ui.util.rememberNavigationState
import com.yagubogu.ui.util.toEntries

@Composable
fun MainScreen(
    onBadgeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedItem: BottomNavKey by rememberSaveable(stateSaver = BottomNavKey.keySaver) {
        mutableStateOf(BottomNavKey.Home)
    }

    val navigationState: NavigationState =
        rememberNavigationState(
            startRoute = BottomNavKey.Home,
            topLevelRoutes = BottomNavKey.items.toSet(),
        )
    val navigator: Navigator = remember { Navigator(navigationState) }

    val selectedItemLabel: String = stringResource(selectedItem.label)
    LaunchedEffect(selectedItem) {
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "$selectedItemLabel 화면")
        }
    }

    Scaffold(
        containerColor = Gray050,
        topBar = {
            MainToolbar(
                title =
                    stringResource(
                        when (selectedItem) {
                            BottomNavKey.Home -> R.string.app_name
                            BottomNavKey.Livetalk,
                            BottomNavKey.Stats,
                            BottomNavKey.AttendanceHistory,
                            -> selectedItem.label
                        },
                    ),
                onBadgeClick = onBadgeClick,
                onSettingsClick = onSettingsClick,
            )
        },
        bottomBar = {
            MainNavigationBar(
                selectedItem = selectedItem,
                onItemClick = { item: BottomNavKey ->
                    navigator.navigate(item)
                    selectedItem = item
                },
            )

            NavigationBar(containerColor = White) {
                BottomNavKey.items.forEach { item: BottomNavKey ->
                    NavigationBarItem(
                        selected = selectedItem == item,
                        onClick = {
                            selectedItem = item
                            navigator.navigate(selectedItem)
                        },
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = stringResource(item.label),
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(item.label),
                                style = PretendardSemiBold12,
                            )
                        },
                        colors =
                            NavigationBarItemColors(
                                selectedIconColor = Primary500,
                                selectedTextColor = Primary500,
                                selectedIndicatorColor = Color.Transparent,
                                unselectedIconColor = Gray500,
                                unselectedTextColor = Gray500,
                                disabledIconColor = Gray500,
                                disabledTextColor = Gray500,
                            ),
                    )
                }
            }
        },
    ) { innerPadding: PaddingValues ->
        val entryProvider: (NavKey) -> NavEntry<NavKey> =
            entryProvider {
                entry<BottomNavKey.Home> { StatsScreen() }
                entry<BottomNavKey.Livetalk> { TODO("LivetalkScreen()") }
                entry<BottomNavKey.Stats> { StatsScreen() }
                entry<BottomNavKey.AttendanceHistory> { TODO("AttendanceHistoryScreen()") }
            }

        NavDisplay(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            entries = navigationState.toEntries(entryProvider),
            onBack = { navigator.goBack() },
        )
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    MainScreen(onBadgeClick = {}, onSettingsClick = {})
}
