package com.yagubogu.ui.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.yagubogu.R
import com.yagubogu.ui.dialog.model.MemberProfile
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.Primary600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDialog(
    onDismissRequest: () -> Unit,
    memberProfile: MemberProfile,
    modifier: Modifier = Modifier,
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier =
            modifier
                .fillMaxWidth(0.85f)
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(20.dp),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        content = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CancelButton(onClick = onDismissRequest)
                ProfileHeader(memberProfile)
                HorizontalDivider(
                    thickness = 0.4.dp,
                    color = Gray300,
                    modifier = modifier.padding(vertical = 12.dp),
                )
                ProfileContent(memberProfile)
            }
        },
    )
}

@Composable
private fun CancelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "닫기",
            tint = Gray500,
            modifier = Modifier.clickable(onClick = onClick),
        )
    }
}

@Composable
private fun ProfileHeader(
    memberProfile: MemberProfile,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 8.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_background),
            contentDescription = "프로필 이미지",
            modifier =
                Modifier
                    .size(100.dp)
                    .clip(CircleShape),
        )
        Spacer(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(20.dp),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Absolute.SpaceBetween,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 9.dp),
        ) {
            Column {
                Text(
                    text = memberProfile.representativeBadgeName,
                    style = PretendardMedium,
                    fontSize = 14.sp,
                    color = Primary600,
                )
                Text(text = memberProfile.nickname, style = PretendardSemiBold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "가입한 날 : 2025.08.14",
                    style = PretendardRegular12,
                    color = Gray500,
                )
            }
            Image(
                painter = painterResource(R.drawable.img_badge_lock),
                contentDescription = "배지 이미지",
                modifier = Modifier.size(64.dp),
            )
        }
    }
}

@Composable
private fun ProfileContent(
    memberProfile: MemberProfile,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 12.dp)) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f, true),
            ) {
                Text(text = "\uD83C\uDF96\uFE0F", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.all_ranking, memberProfile.victoryFairyRanking),
                    style = PretendardSemiBold,
                    fontSize = 20.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "승리 요정 랭킹", style = PretendardRegular12, color = Gray500)
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
                Text(text = "\uD83E\uDDDA", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.all_score, memberProfile.victoryFairyScore),
                    style = PretendardSemiBold,
                    fontSize = 20.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "승리 요정 점수", style = PretendardRegular12, color = Gray500)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f, true),
            ) {
                Text(text = memberProfile.favoriteTeam, style = PretendardBold20)
                Spacer(modifier = Modifier.height(4.dp))
                Text("응원 팀", style = PretendardRegular12, color = Gray500)
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
                Text(text = "25", style = PretendardBold20)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "인증 횟수", style = PretendardRegular12, color = Gray500)
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
                Text(text = "75%", style = PretendardBold20)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "직관 승률", style = PretendardRegular12, color = Gray500)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileDialogPreview() {
    ProfileDialog(
        onDismissRequest = {},
        memberProfile =
            MemberProfile(
                nickname = "귀여운보욱이",
                enterDate = "2025-08-22",
                profileImageUrl = "",
                favoriteTeam = "KIA",
                representativeBadgeName = "말문이 트이다",
                representativeBadgeImageUrl = "",
                victoryFairyRanking = 275,
                victoryFairyScore = 33,
                checkInCounts = 11,
                checkInWinRate = "60%",
            ),
    )
}
