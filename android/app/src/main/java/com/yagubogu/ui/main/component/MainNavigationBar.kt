package com.yagubogu.ui.main.component

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.main.BottomNavKey
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardSemiBold12
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun MainNavigationBar(
    selectedItem: BottomNavKey,
    onItemClick: (BottomNavKey) -> Unit,
    onItemReselect: (BottomNavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(containerColor = White) {
        BottomNavKey.items.forEach { item: BottomNavKey ->
            NavigationBarItem(
                selected = selectedItem == item,
                onClick = {
                    if (selectedItem == item) {
                        onItemReselect(item)
                    } else {
                        onItemClick(item)
                    }
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
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary500,
                        selectedTextColor = Primary500,
                        unselectedIconColor = Gray500,
                        unselectedTextColor = Gray500,
                        indicatorColor = White,
                    ),
                interactionSource =
                    object : MutableInteractionSource { // Ripple 효과 제거 위함
                        override suspend fun emit(interaction: Interaction) {}

                        override fun tryEmit(interaction: Interaction): Boolean = true

                        override val interactions: Flow<Interaction> = emptyFlow()
                    },
            )
        }
    }
}

@Preview
@Composable
private fun MainNavigationBarPreview() {
    MainNavigationBar(
        selectedItem = BottomNavKey.Home,
        onItemClick = {},
        onItemReselect = {},
    )
}
