package com.yagubogu.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.R
import com.yagubogu.ui.theme.Black
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.PretendardBold20

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Toolbar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CenterAlignedTopAppBar(
        title = { Text(text = title, style = PretendardBold20) },
        navigationIcon = {
            IconButton(
                modifier =
                    Modifier
                        .padding(start = 4.dp),
                onClick = onBackClick,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_left),
                    contentDescription = stringResource(R.string.all_back_button_content_description),
                )
            }
        },
        colors =
            TopAppBarColors(
                containerColor = Gray050,
                navigationIconContentColor = Black,
                titleContentColor = Black,
                actionIconContentColor = Black,
                scrolledContainerColor = Gray050,
            ),
        modifier = modifier,
    )
}

@Preview
@Composable
private fun ToolbarPreview() {
    Toolbar("탑바 제목", onBackClick = {})
}
