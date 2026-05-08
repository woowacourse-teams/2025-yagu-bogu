package com.yagubogu.ui.common.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yagubogu.ui.common.AdUnitIds
import com.yagubogu.ui.theme.EsamanruMedium20
import com.yagubogu.ui.theme.Gray200
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.Primary500
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.all_cancel
import yagubogu.composeapp.generated.resources.exit_dialog_confirm
import yagubogu.composeapp.generated.resources.exit_dialog_title

@Composable
fun ExitConfirmDialog(
    onExit: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false,
            ),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = stringResource(Res.string.exit_dialog_title),
                    style = EsamanruMedium20,
                    textAlign = TextAlign.Center,
                )
                BannerAd(
                    adUnitId = AdUnitIds.exitDialogBanner,
                    bannerAdType = BannerAdType.MEDIUM_RECTANGLE,
                    backgroundColor = Transparent,
                    modifier = Modifier.requiredWidth(300.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Gray200,
                                contentColor = Gray500,
                            ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.all_cancel),
                            style = PretendardSemiBold.copy(fontSize = 14.sp),
                        )
                    }
                    Button(
                        onClick = onExit,
                        modifier = Modifier.weight(1f),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Primary500,
                                contentColor = Color.White,
                            ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.exit_dialog_confirm),
                            style = PretendardSemiBold.copy(fontSize = 14.sp),
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ExitConfirmDialogPreview() {
    ExitConfirmDialog(
        onExit = {},
        onDismiss = {},
    )
}
