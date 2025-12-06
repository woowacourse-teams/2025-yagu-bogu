package com.yagubogu.ui.main

import androidx.annotation.StringRes
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.yagubogu.R
import com.yagubogu.ui.main.component.MainToolbar
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardSemiBold12
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var selectedItem by rememberSaveable(stateSaver = BottomNavKey.keySaver) {
        mutableStateOf(BottomNavKey.Home)
    }

    val backStack = rememberNavBackStack(BottomNavKey.Home)

    @StringRes
    val titleResId: Int =
        when (selectedItem) {
            BottomNavKey.Home -> R.string.app_name
            BottomNavKey.Livetalk -> R.string.bottom_navigation_livetalk
            BottomNavKey.Stats -> R.string.bottom_navigation_stats
            BottomNavKey.AttendanceHistory -> R.string.bottom_navigation_attendance_history
        }

    Scaffold(
        topBar = {
            MainToolbar(
                title = stringResource(titleResId),
                onBadgeClick = { },
                onSettingsClick = { },
            )
        },
        containerColor = Gray050,
        bottomBar = {
            NavigationBar(containerColor = White) {
                BottomNavKey.items.forEach { item: BottomNavKey ->
                    NavigationBarItem(
                        selected = selectedItem == item,
                        onClick = {
                            backStack.clear()
                            backStack.add(item)
                            selectedItem = item
                        },
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = stringResource(item.title),
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(item.title),
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
        NavDisplay(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            backStack = backStack,
            entryProvider =
                entryProvider {
                    // TODO: 다른 화면 모두 추가
                    entry<BottomNavKey.Stats> {
//                        StatsScreen(statsMyViewModel, statsDetailViewModel)
                    }
                },
        )
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    MainScreen()
}
