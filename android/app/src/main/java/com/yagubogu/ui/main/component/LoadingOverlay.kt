package com.yagubogu.ui.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.yagubogu.ui.theme.Dimming050
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.util.noRippleClickable

@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!isLoading) return
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Dimming050)
                .noRippleClickable {},
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = Primary500,
        )
    }
}
