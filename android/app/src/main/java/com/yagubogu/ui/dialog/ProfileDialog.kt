package com.yagubogu.ui.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.yagubogu.R
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.Primary600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDialog(modifier: Modifier = Modifier) {
    BasicAlertDialog(
        onDismissRequest = {},
        modifier =
            modifier
                .fillMaxWidth(0.9f)
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(20.dp),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_background),
                    contentDescription = "프로필 이미지",
                    modifier = modifier.clip(CircleShape),
                )
                ProfileHeader()
                HorizontalDivider(
                    thickness = 0.4.dp,
                    color = Gray300,
                    modifier = modifier.padding(vertical = 12.dp),
                )
                ProfileContent()
            }
        },
    )
}

@Composable
fun ProfileHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(top = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                "말문이 트이다",
                style = PretendardMedium,
                fontSize = 14.sp,
                color = Primary600,
            )
            Text("우가우가귀여운보욱이우가", style = PretendardSemiBold, fontSize = 20.sp)
            Spacer(modifier = modifier.height(12.dp))
            Text(
                "가입한 날 : 2025.08.14",
                style = PretendardRegular12,
                color = Gray500,
            )
        }
        Image(
            painter = painterResource(R.drawable.img_badge_lock),
            contentDescription = "배지 이미지",
            modifier = modifier.size(64.dp),
        )
    }
}

@Composable
fun ProfileContent(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = 10.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f, true),
        ) {
            Text("\uD83C\uDF96\uFE0F", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("275등", style = PretendardSemiBold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("승리 요정 랭킹", style = PretendardRegular12)
        }
        VerticalDivider(
            thickness = 0.4.dp,
            color = Gray300,
            modifier = Modifier.height(54.dp),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f, true),
        ) {
            Text("\uD83E\uDDDA", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("33점", style = PretendardSemiBold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("승리 요정 점수", style = PretendardRegular12)
        }
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = 10.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f, true),
        ) {
            Text("KIA", style = PretendardBold20)
            Spacer(modifier = Modifier.height(4.dp))
            Text("응원 팀", style = PretendardRegular12)
        }
        VerticalDivider(
            thickness = 0.4.dp,
            color = Gray300,
            modifier = Modifier.height(34.dp),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f, true),
        ) {
            Text("25", style = PretendardBold20)
            Spacer(modifier = Modifier.height(4.dp))
            Text("인증 횟수", style = PretendardRegular12)
        }
        VerticalDivider(
            thickness = 0.4.dp,
            color = Gray300,
            modifier = Modifier.height(34.dp),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f, true),
        ) {
            Text("75%", style = PretendardBold20)
            Spacer(modifier = Modifier.height(4.dp))
            Text("직관 승률", style = PretendardRegular12)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileDialogPreview() {
    ProfileDialog()
}
