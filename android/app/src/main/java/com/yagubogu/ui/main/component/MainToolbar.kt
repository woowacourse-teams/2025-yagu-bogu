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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.R
import com.yagubogu.ui.theme.Black
import com.yagubogu.ui.theme.EsamanruMedium24
import com.yagubogu.ui.theme.Gray050

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainToolbar(
    title: String,
    onBadgeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text(text = title, style = EsamanruMedium24) },
        actions = {
            IconButton(onClick = onBadgeClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_trophy),
                    contentDescription = stringResource(R.string.badge_icon_description),
                    modifier = Modifier.size(24.dp),
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = stringResource(R.string.setting_icon_description),
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
