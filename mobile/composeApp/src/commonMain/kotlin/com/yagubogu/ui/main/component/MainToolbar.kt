package com.yagubogu.ui.main.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.theme.Black
import com.yagubogu.ui.theme.EsamanruMedium24
import com.yagubogu.ui.theme.Gray050
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.badge_icon_description
import yagubogu.composeapp.generated.resources.ic_settings
import yagubogu.composeapp.generated.resources.ic_trophy
import yagubogu.composeapp.generated.resources.setting_icon_description

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainToolbar(
    title: String,
    onBadgeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(text = title, style = EsamanruMedium24) },
        actions = {
            IconButton(onClick = onBadgeClick) {
                Icon(
                    painter = painterResource(Res.drawable.ic_trophy),
                    contentDescription = stringResource(Res.string.badge_icon_description),
                    modifier = Modifier.size(24.dp),
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    painter = painterResource(Res.drawable.ic_settings),
                    contentDescription = stringResource(Res.string.setting_icon_description),
                    modifier = Modifier.size(24.dp),
                )
            }
        },
        colors =
            TopAppBarColors(
                containerColor = Gray050,
                scrolledContainerColor = Gray050,
                navigationIconContentColor = Black,
                titleContentColor = Black,
                subtitleContentColor = Black,
                actionIconContentColor = Black,
            ),
    )
}

@Preview
@Composable
private fun MainToolbarPreview() {
    MainToolbar(
        title = "야구보구",
        onBadgeClick = {},
        onSettingsClick = {},
    )
}
