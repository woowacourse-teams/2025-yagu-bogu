package com.yagubogu.ui.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yagubogu.R
import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Latitude
import com.yagubogu.domain.model.Longitude
import com.yagubogu.presentation.home.model.Stadium
import com.yagubogu.ui.theme.EsamanruMedium20
import com.yagubogu.ui.theme.Gray200
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.Gray700
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.Primary500

@Composable
fun DoubleHeaderDialog(
    stadium: Stadium,
    onConfirm: (Long) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(dismissOnClickOutside = false),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.home_check_in_stadium_emoji),
                    style = TextStyle(fontSize = 48.sp),
                )
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.home_double_header_title, stadium.name),
                    style = EsamanruMedium20,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.home_double_header_message),
                    style = PretendardMedium.copy(fontSize = 14.sp),
                    textAlign = TextAlign.Center,
                    color = Gray700,
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    stadium.gameIds.forEachIndexed { index: Int, gameId: Long ->
                        Button(
                            onClick = { onConfirm(gameId) },
                            modifier = Modifier.weight(1f),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = Primary500,
                                    contentColor = Color.White,
                                ),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(
                                text = "${index + 1}차전",
                                style = PretendardSemiBold.copy(fontSize = 14.sp),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onCancel,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Gray200,
                            contentColor = Gray500,
                        ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.all_cancel),
                        style = PretendardSemiBold.copy(fontSize = 14.sp),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun DoubleHeaderDialogPreview() {
    DoubleHeaderDialog(
        stadium =
            Stadium(
                name = "잠실야구장",
                coordinate = Coordinate(Latitude(0.0), Longitude(0.0)),
                gameIds = listOf(0, 1),
            ),
        onConfirm = {},
        onCancel = {},
    )
}
