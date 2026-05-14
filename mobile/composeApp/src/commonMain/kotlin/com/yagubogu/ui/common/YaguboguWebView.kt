package com.yagubogu.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import com.yagubogu.ui.theme.Gray050

@Composable
fun YaguboguWebView(
    url: String,
    modifier: Modifier =
        Modifier.padding(
            top = 8.dp,
            start = 20.dp,
            end = 20.dp,
            bottom = 20.dp,
        ),
) {
    val webViewState =
        rememberWebViewState(url).also { state ->
            state.webSettings.iOSWebSettings.apply {
                opaque = false
                backgroundColor = Gray050
                underPageBackgroundColor = Gray050
            }
            state.webSettings.androidWebSettings.apply {
                isAlgorithmicDarkeningAllowed = false
            }
        }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050),
    ) {
        WebView(
            state = webViewState,
            modifier = Modifier.fillMaxSize(),
        )

        if (webViewState.loadingState is LoadingState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}
