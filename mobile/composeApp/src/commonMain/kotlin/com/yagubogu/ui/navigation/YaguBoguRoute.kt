@file:OptIn(ExperimentalSerializationApi::class)

package com.yagubogu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.yagubogu.ui.navigation.model.BottomNavKey
import com.yagubogu.ui.navigation.model.NavigationState
import com.yagubogu.ui.navigation.model.Navigator
import com.yagubogu.ui.navigation.model.Route
import com.yagubogu.ui.navigation.model.SettingNavKey
import com.yagubogu.ui.navigation.model.rememberNavigationState
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * 앱의 최상위 라우팅 컴포저블.
 *
 * 각 네비게이션 상태를 별도로 관리하여 스택 추적 독립성을 보장합니다.
 * [rootNavigator]: 최상위 라우팅 관리
 * [mainNavigator]: 하단 탭 네비게이션
 * [settingNavigator]: 설정 화면 네비게이션
 *
 * @param startRoute 앱 시작 시 표시할 화면
 * @param modifier 레이아웃 수정을 위한 [Modifier]
 */
@Composable
fun YaguBoguRoute(
    startRoute: Route,
    modifier: Modifier = Modifier,
) {
    val rootNavigationState: NavigationState =
        rememberNavigationState(
            startRoute = startRoute,
            topLevelRoutes =
                setOf(
                    Route.Main,
                    Route.Login,
                ),
            savedStateConfig = rootSavedStateConfig,
        )
    val rootNavigator: Navigator = remember { Navigator(rootNavigationState) }

    val mainNavigationState: NavigationState =
        rememberNavigationState(
            startRoute = BottomNavKey.Home,
            topLevelRoutes = BottomNavKey.items.toSet(),
            savedStateConfig = mainSavedStateConfig,
        )
    val mainNavigator: Navigator = remember { Navigator(mainNavigationState) }

    val settingNavigationState: NavigationState =
        rememberNavigationState(
            startRoute = SettingNavKey.SettingMain,
            topLevelRoutes = setOf(SettingNavKey.SettingMain),
            savedStateConfig = settingSavedStateConfig,
        )
    val settingNavigator: Navigator = remember { Navigator(settingNavigationState) }

    NavigationRoot(
        rootNavigator = rootNavigator,
        mainNavigator = mainNavigator,
        settingNavigator = settingNavigator,
        modifier = modifier,
    )
}

private val rootSavedStateConfig =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclassesOfSealed<Route>()
                }
            }
    }

private val mainSavedStateConfig =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclassesOfSealed<BottomNavKey>()
                }
            }
    }

private val settingSavedStateConfig =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclassesOfSealed<SettingNavKey>()
                }
            }
    }
