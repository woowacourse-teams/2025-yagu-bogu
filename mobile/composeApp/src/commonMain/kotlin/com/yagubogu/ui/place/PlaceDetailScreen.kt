package com.yagubogu.ui.place

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.ui.common.component.DefaultToolbar
import com.yagubogu.ui.common.component.PlaceMapView
import com.yagubogu.ui.place.model.PlaceCategory
import com.yagubogu.ui.place.model.labelResource
import com.yagubogu.ui.theme.Gold
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray100
import com.yagubogu.ui.theme.Gray300
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.Gray600
import com.yagubogu.ui.theme.Gray900
import com.yagubogu.ui.theme.PretendardBold
import com.yagubogu.ui.theme.PretendardBold20
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardMedium12
import com.yagubogu.ui.theme.PretendardMedium16
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardRegular16
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.rememberNoRippleInteractionSource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.ic_clock
import yagubogu.composeapp.generated.resources.ic_compass
import yagubogu.composeapp.generated.resources.ic_copy
import yagubogu.composeapp.generated.resources.ic_phone
import yagubogu.composeapp.generated.resources.ic_walk
import yagubogu.composeapp.generated.resources.place_detail_business_hours
import yagubogu.composeapp.generated.resources.place_detail_copy_address_content_description
import yagubogu.composeapp.generated.resources.place_detail_location_title
import yagubogu.composeapp.generated.resources.place_detail_phone_number
import yagubogu.composeapp.generated.resources.place_detail_title
import yagubogu.composeapp.generated.resources.place_detail_visitor_standard

@Composable
fun PlaceDetailScreen(
    placeName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val placeDetail: PlaceDetailUiModel = rememberPlaceDetailUiModel(placeName)

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
        Column(
            modifier =
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Gray050)
                    .verticalScroll(rememberScrollState()),
        ) {
            PlaceHeroImage(placeDetail = placeDetail)
            PlaceDetailHeader(placeDetail = placeDetail)
            PlaceDescription(description = placeDetail.description)
            PlaceLocationSection(placeDetail = placeDetail)
            PlaceInfoCards(placeDetail = placeDetail)
        }
    }
}

@Composable
private fun PlaceHeroImage(
    placeDetail: PlaceDetailUiModel,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(placeDetail.heroStartColor, placeDetail.heroEndColor),
                    ),
                ),
    ) {
        Box(
            modifier =
                Modifier
                    .size(160.dp)
                    .align(Alignment.Center)
                    .background(White.copy(alpha = 0.16f), CircleShape),
        )
        Box(
            modifier =
                Modifier
                    .size(92.dp)
                    .align(Alignment.Center)
                    .background(White.copy(alpha = 0.24f), CircleShape),
        )
        Text(
            text = placeDetail.heroText,
            style = PretendardBold.copy(fontSize = 30.sp),
            color = White,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun PlaceDetailHeader(
    placeDetail: PlaceDetailUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(White)
                .padding(start = 20.dp, top = 20.dp, end = 20.dp),
    ) {
        PlaceCategoryLabel(label = stringResource(placeDetail.category.labelResource))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = placeDetail.name,
            style = PretendardBold.copy(fontSize = 24.sp, lineHeight = 28.sp),
            color = Gray900,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(Res.drawable.ic_walk),
                contentDescription = null,
                tint = Gray600,
                modifier = Modifier.size(width = 12.dp, height = 18.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = placeDetail.walkDistance,
                style = PretendardRegular12,
                color = Gray600,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(Res.string.place_detail_visitor_standard),
                style = PretendardMedium12,
                color = Gray600,
            )
            Spacer(modifier = Modifier.width(8.dp))
            PlaceRankBadge(text = placeDetail.rankLabel)
        }
    }
}

@Composable
private fun PlaceDescription(
    description: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = description,
        style = PretendardMedium.copy(fontSize = 16.sp, lineHeight = 26.sp),
        color = Color(0xFF3D4A3D),
        modifier =
            modifier
                .fillMaxWidth()
                .background(White)
                .padding(20.dp),
    )
}

@Composable
private fun PlaceLocationSection(
    placeDetail: PlaceDetailUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(White)
                .padding(20.dp),
    ) {
        Text(
            text = stringResource(Res.string.place_detail_location_title),
            style = PretendardBold20,
            color = Gray900,
        )
        Spacer(modifier = Modifier.height(12.dp))
        PlaceMapView(
            address = placeDetail.address,
            placeName = placeDetail.name,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(198.dp)
                    .clip(RoundedCornerShape(12.dp)),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_compass),
                contentDescription = null,
                tint = Gray500,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = placeDetail.address,
                    style = PretendardMedium16,
                    color = Gray900,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = placeDetail.addressGuide,
                    style = PretendardMedium12,
                    color = Gray600,
                )
            }
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .background(Gray100, RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = rememberNoRippleInteractionSource(),
                            indication = null,
                            onClick = {},
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_copy),
                    contentDescription = stringResource(Res.string.place_detail_copy_address_content_description),
                    tint = Gray600,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Composable
private fun PlaceInfoCards(
    placeDetail: PlaceDetailUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(White)
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
    ) {
        PlaceInfoCard(
            icon = Res.drawable.ic_clock,
            title = stringResource(Res.string.place_detail_business_hours),
            value = placeDetail.businessHours,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(12.dp))
        PlaceInfoCard(
            icon = Res.drawable.ic_phone,
            title = stringResource(Res.string.place_detail_phone_number),
            value = placeDetail.phoneNumber,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PlaceInfoCard(
    icon: org.jetbrains.compose.resources.DrawableResource,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .background(White, RoundedCornerShape(12.dp))
                .border(1.dp, Gray300, RoundedCornerShape(12.dp))
                .padding(20.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = PretendardMedium.copy(fontSize = 14.sp, lineHeight = 16.sp),
            color = Gray600,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = PretendardRegular16.copy(lineHeight = 20.sp),
            color = Gray900,
        )
    }
}

@Composable
private fun PlaceCategoryLabel(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        style = PretendardMedium12,
        color = Gray500,
        modifier =
            modifier
                .background(Gray100, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
private fun PlaceRankBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = PretendardMedium12,
        color = White,
        modifier =
            modifier
                .background(Gold, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
private fun rememberPlaceDetailUiModel(placeName: String): PlaceDetailUiModel =
    PLACE_DETAIL_ITEMS[placeName]
        ?: PLACE_DETAIL_ITEMS.values.first().copy(name = placeName)

private data class PlaceDetailUiModel(
    val name: String,
    val category: PlaceCategory,
    val walkDistance: String,
    val rankLabel: String,
    val description: String,
    val address: String,
    val addressGuide: String,
    val businessHours: String,
    val phoneNumber: String,
    val heroText: String,
    val heroStartColor: Color,
    val heroEndColor: Color,
)

private val PLACE_DETAIL_ITEMS: Map<String, PlaceDetailUiModel> =
    listOf(
        PlaceDetailUiModel(
            name = "잠실 돈까스 본점",
            category = PlaceCategory.FOOD,
            walkDistance = "도보 320m",
            rankLabel = "맛집 1위",
            description = "경기 전후로 팬들이 가장 많이 찾는 명소입니다. 바삭한 튀김과 깊은 소스 맛으로 이미 잠실 야구장 근처에서는 소문난 맛집입니다. 주문 즉시 조리되어 최고의 맛을 보장합니다.",
            address = "서울 송파구 올림픽로 25",
            addressGuide = "야구장 서문에서 도보 5분 거리",
            businessHours = "11:00 - 22:00",
            phoneNumber = "02-123-4567",
            heroText = "돈까스",
            heroStartColor = Color(0xFFE8A444),
            heroEndColor = Color(0xFFB86A2A),
        ),
        PlaceDetailUiModel(
            name = "승리 떡볶이",
            category = PlaceCategory.FOOD,
            walkDistance = "도보 450m",
            rankLabel = "맛집 2위",
            description = "매콤달콤한 떡볶이와 바삭한 튀김을 함께 즐길 수 있는 경기장 근처 인기 분식집입니다. 경기 시작 전 빠르게 들르기 좋아요.",
            address = "서울 송파구 백제고분로 12",
            addressGuide = "야구장 남문에서 도보 7분 거리",
            businessHours = "10:30 - 21:30",
            phoneNumber = "02-222-4567",
            heroText = "분식",
            heroStartColor = Color(0xFFE85D4A),
            heroEndColor = Color(0xFFB72D2A),
        ),
        PlaceDetailUiModel(
            name = "홈런 한우구이",
            category = PlaceCategory.FOOD,
            walkDistance = "도보 680m",
            rankLabel = "맛집 3위",
            description = "두툼한 한우구이와 든든한 식사 메뉴가 준비된 회식형 맛집입니다. 경기 후 여유 있게 승리의 기분을 나누기 좋은 장소입니다.",
            address = "서울 송파구 올림픽로 32길 8",
            addressGuide = "야구장 동문에서 도보 10분 거리",
            businessHours = "12:00 - 23:00",
            phoneNumber = "02-333-4567",
            heroText = "한우",
            heroStartColor = Color(0xFF8F5A3C),
            heroEndColor = Color(0xFF4A2A1C),
        ),
        PlaceDetailUiModel(
            name = "카페 베이스런",
            category = PlaceCategory.FOOD,
            walkDistance = "도보 810m",
            rankLabel = "맛집 4위",
            description = "야구장 근처에서 잠시 쉬어가기 좋은 카페입니다. 넓은 좌석과 고소한 커피, 간단한 디저트 메뉴가 준비되어 있습니다.",
            address = "서울 송파구 올림픽로 45",
            addressGuide = "야구장 북문에서 도보 12분 거리",
            businessHours = "09:00 - 22:00",
            phoneNumber = "02-444-4567",
            heroText = "카페",
            heroStartColor = Color(0xFF6B8F71),
            heroEndColor = Color(0xFF315A42),
        ),
    ).associateBy { it.name }

@Preview(showBackground = true)
@Composable
private fun PlaceDetailScreenPreview() {
    PlaceDetailScreen(
        placeName = "잠실 야구장",
        onBackClick = {},
    )
}
