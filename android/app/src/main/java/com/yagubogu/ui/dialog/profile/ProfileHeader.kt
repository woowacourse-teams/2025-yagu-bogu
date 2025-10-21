package com.yagubogu.ui.dialog.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.yagubogu.R
import com.yagubogu.ui.dialog.model.MEMBER_PROFILE_FIXTURE
import com.yagubogu.ui.dialog.model.MemberProfile
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardRegular
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.PretendardSemiBold20
import com.yagubogu.ui.theme.Primary600
import com.yagubogu.ui.theme.Primary700

@Composable
fun ProfileHeader(
    memberProfile: MemberProfile,
    modifier: Modifier = Modifier,
) {
    val profileImageSize = 100.dp
    val profileImageOverlap = (0.6 * profileImageSize.value).dp // 카드 위로 겹치는 영역

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier =
                    modifier
                        .padding(top = profileImageOverlap)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(
                            top = profileImageOverlap - 20.dp,
                            bottom = 20.dp,
                            start = 20.dp,
                            end = 20.dp,
                        ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(horizontal = 9.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = stringResource(R.string.all_fan, memberProfile.favoriteTeam),
                            style = PretendardMedium,
                            fontSize = 12.sp,
                            color = Primary600,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = memberProfile.nickname,
                            style = PretendardSemiBold20,
                            maxLines = 1,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        RankingText(memberProfile)
                    }
                    memberProfile.representativeBadgeImageUrl?.let { badgeImageUrl: String ->
                        AsyncImage(
                            model = badgeImageUrl,
                            contentDescription = "대표 배지 이미지",
                            modifier =
                                Modifier
                                    .size(60.dp)
                                    .padding(start = 8.dp),
                        )
                    }
                }
            }
            Box(
                modifier =
                    modifier
                        .size(100.dp)
                        .background(Gray050, CircleShape)
                        .padding(10.dp)
                        .clip(CircleShape),
            ) {
                AsyncImage(
                    model = memberProfile.profileImageUrl,
                    contentDescription = "프로필 이미지",
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun RankingText(
    memberProfile: MemberProfile,
    modifier: Modifier = Modifier,
) {
    Row {
        Text(text = stringResource(R.string.profile_ranking_all), style = PretendardRegular, fontSize = 10.sp, color = Gray500)
        Text(
            text = memberProfile.victoryFairyRanking?.toString() ?: "-",
            style = PretendardSemiBold,
            fontSize = 10.sp,
            color = Primary700,
        )
        Text(text = stringResource(R.string.profile_ranking_rank), style = PretendardRegular, fontSize = 10.sp, color = Gray500)
        Text(
            text = "|",
            style = PretendardRegular,
            fontSize = 10.sp,
            color = Gray500,
            modifier = modifier.padding(horizontal = 6.dp),
        )
        Text(
            text = stringResource(R.string.all_fan, memberProfile.favoriteTeam),
            style = PretendardRegular,
            fontSize = 10.sp,
            color = Gray500,
        )
        Text(text = stringResource(R.string.profile_ranking_within_team), style = PretendardRegular, fontSize = 10.sp, color = Gray500)
        Text(
            text = memberProfile.victoryFairyRankingWithinTeam?.toString() ?: "-",
            style = PretendardSemiBold,
            fontSize = 10.sp,
            color = Primary700,
        )
        Text(text = "위", style = PretendardRegular, fontSize = 10.sp, color = Gray500)
    }
}

@Preview
@Composable
private fun ProfileHeaderPreview(modifier: Modifier = Modifier) {
    ProfileHeader(
        memberProfile = MEMBER_PROFILE_FIXTURE,
        modifier = modifier,
    )
}
