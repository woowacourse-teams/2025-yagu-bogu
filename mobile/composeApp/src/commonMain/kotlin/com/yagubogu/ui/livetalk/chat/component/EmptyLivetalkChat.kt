package com.yagubogu.ui.livetalk.chat.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.PretendardMedium
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.img_baseball_livetalk_empty
import yagubogu.composeapp.generated.resources.livetalk_empty_game_illustration_description
import yagubogu.composeapp.generated.resources.livetalk_empty_livetalk_description
import yagubogu.composeapp.generated.resources.livetalk_empty_not_check_in_livetalk_description

@Composable
fun EmptyLivetalkChat(
    modifier: Modifier = Modifier,
    isCheckIn: Boolean = false,
) {
    Column(
        modifier =
            modifier
                .background(Gray050),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.img_baseball_livetalk_empty),
            contentDescription = stringResource(Res.string.livetalk_empty_game_illustration_description),
            modifier =
                Modifier
                    .height(241.dp)
                    .fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text =
                stringResource(
                    when (isCheckIn) {
                        true -> Res.string.livetalk_empty_livetalk_description
                        false -> Res.string.livetalk_empty_not_check_in_livetalk_description
                    },
                ),
            textAlign = TextAlign.Center,
            style = PretendardMedium.copy(fontSize = 18.sp, color = Gray400),
        )
    }
}

@Preview
@Composable
private fun EmptyLivetalkScreenPreview() {
    EmptyLivetalkChat(isCheckIn = true)
}

@Preview
@Composable
private fun EmptyNotCheckInLivetalkScreenPreview() {
    EmptyLivetalkChat()
}
