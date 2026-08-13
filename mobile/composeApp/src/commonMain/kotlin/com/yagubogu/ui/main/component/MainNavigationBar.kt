package com.yagubogu.ui.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.navigation.model.BottomNavKey
import com.yagubogu.ui.theme.Gray200
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardSemiBold12
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.rememberNoRippleInteractionSource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MainNavigationBar(
    selectedItem: BottomNavKey,
    onItemClick: (BottomNavKey) -> Unit,
    onItemReselect: (BottomNavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        HorizontalDivider(color = Gray200, thickness = 0.4.dp)

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(White)
                    .navigationBarsPadding()
                    .padding(top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomNavKey.items.forEach { item: BottomNavKey ->
                val isSelected: Boolean = selectedItem == item
                val contentColor: Color = if (isSelected) Primary500 else Gray500

                Column(
                    modifier =
                        Modifier
                            .clickable(
                                interactionSource = rememberNoRippleInteractionSource(),
                                onClick = {
                                    if (isSelected) onItemReselect(item) else onItemClick(item)
                                },
                            ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = stringResource(item.label),
                        modifier = Modifier.size(24.dp),
                        tint = contentColor,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(item.label),
                        style = PretendardSemiBold12,
                        color = contentColor,
                    )
                }
            }
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
