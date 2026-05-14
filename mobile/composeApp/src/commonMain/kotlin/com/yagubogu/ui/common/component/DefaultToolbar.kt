package com.yagubogu.ui.common.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.ui.theme.Black
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.PretendardBold20
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_back_button_content_description
import yagubogu.composeapp.generated.resources.ic_arrow_left

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultToolbar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "",
    actions: @Composable (RowScope.() -> Unit) = {},
) {
    CenterAlignedTopAppBar(
        title = { Text(text = title, style = PretendardBold20) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_left),
                    contentDescription = stringResource(Res.string.all_back_button_content_description),
                )
            }
        },
        colors =
            TopAppBarColors(
                containerColor = Gray050,
                navigationIconContentColor = Black,
                titleContentColor = Black,
                subtitleContentColor = Black,
                actionIconContentColor = Black,
                scrolledContainerColor = Gray050,
            ),
        actions = actions,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun DefaultToolbarPreview() {
    DefaultToolbar(onBackClick = {})
}
