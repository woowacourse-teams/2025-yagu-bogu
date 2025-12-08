package com.yagubogu.ui.main.component

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

@Composable
fun MainNavigationBar(
    selectedItem: BottomNavKey,
    onItemClick: (BottomNavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(containerColor = White) {
        BottomNavKey.items.forEach { item: BottomNavKey ->
            NavigationBarItem(
                selected = selectedItem == item,
                onClick = { onItemClick(item) },
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
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary500,
                        selectedTextColor = Primary500,
                        unselectedIconColor = Gray500,
                        unselectedTextColor = Gray500,
                        indicatorColor = White,
                    ),
                interactionSource = MutableInteractionSource(),
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
    )
}
