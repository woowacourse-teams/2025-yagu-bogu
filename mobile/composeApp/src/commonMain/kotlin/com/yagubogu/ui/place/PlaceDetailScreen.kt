package com.yagubogu.ui.place

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.yagubogu.ui.common.component.DefaultToolbar
import com.yagubogu.ui.theme.Gray050
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.place_detail_title

@Composable
fun PlaceDetailScreen(
    placeName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            DefaultToolbar(
                title = stringResource(Res.string.place_detail_title, placeName),
                onBackClick = onBackClick,
            )
        },
        containerColor = Gray050,
        modifier = modifier,
    ) { innerPadding: PaddingValues ->
        Box(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Gray050),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceDetailScreenPreview() {
    PlaceDetailScreen(
        placeName = "잠실 야구장",
        onBackClick = {},
    )
}
