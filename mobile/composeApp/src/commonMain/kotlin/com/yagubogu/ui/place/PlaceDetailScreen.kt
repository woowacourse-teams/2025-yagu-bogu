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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.yagubogu.ui.common.component.DefaultToolbar
import com.yagubogu.ui.common.component.PlaceMapView
import com.yagubogu.ui.mapper.formatDistanceMeters
import com.yagubogu.ui.place.model.PlaceCategory
import com.yagubogu.ui.place.model.PlaceDetailRow
import com.yagubogu.ui.place.model.PlaceDetailUiModel
import com.yagubogu.ui.place.model.PlaceDetailUiState
import com.yagubogu.ui.place.model.labelResource
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
import com.yagubogu.ui.theme.Primary500
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.LocalSnackbarHostState
import com.yagubogu.ui.util.LocalSnackbarScope
import com.yagubogu.ui.util.categoryThumbnailColor
import com.yagubogu.ui.util.rememberNoRippleInteractionSource
import com.yagubogu.ui.util.showSingleSnackbar
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.ic_clock
import yagubogu.composeapp.generated.resources.ic_compass
import yagubogu.composeapp.generated.resources.ic_copy
import yagubogu.composeapp.generated.resources.ic_globe_location_pin
import yagubogu.composeapp.generated.resources.ic_phone
import yagubogu.composeapp.generated.resources.ic_walk
import yagubogu.composeapp.generated.resources.place_detail_address_copied
import yagubogu.composeapp.generated.resources.place_detail_business_hours
import yagubogu.composeapp.generated.resources.place_detail_copy_address_content_description
import yagubogu.composeapp.generated.resources.place_detail_extra_info_title
import yagubogu.composeapp.generated.resources.place_detail_field_firstmenu
import yagubogu.composeapp.generated.resources.place_detail_field_packing
import yagubogu.composeapp.generated.resources.place_detail_field_smoking
import yagubogu.composeapp.generated.resources.place_detail_homepage_title
import yagubogu.composeapp.generated.resources.place_detail_location_title
import yagubogu.composeapp.generated.resources.place_detail_phone_copied
import yagubogu.composeapp.generated.resources.place_detail_phone_number
import yagubogu.composeapp.generated.resources.place_list_error

@Composable
fun PlaceDetailScreen(
    placeId: Long,
    placeName: String,
    distanceMeters: Int?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaceViewModel = koinViewModel(),
) {
    val placeDetailUiState: PlaceDetailUiState by viewModel.placeDetail.collectAsStateWithLifecycle()

    LaunchedEffect(placeId) {
        viewModel.loadPlaceDetail(placeId, distanceMeters)
    }

    // CompositionLocal은 미리보기(Preview)에서 제공되지 않으므로, 상태(ViewModel)를 읽는
    // 이 최상위 컴포저블에서만 소비하고 아래로는 순수 콜백으로 전달한다.
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = LocalSnackbarHostState.current
    val snackbarScope = LocalSnackbarScope.current
    val addressCopiedMessage: String = stringResource(Res.string.place_detail_address_copied)
    val phoneCopiedMessage: String = stringResource(Res.string.place_detail_phone_copied)

    PlaceDetailScreen(
        placeName = placeName,
        placeDetailUiState = placeDetailUiState,
        onBackClick = onBackClick,
        onCopyAddress = { address: String ->
            clipboardManager.setText(AnnotatedString(address))
            snackbarHostState.showSingleSnackbar(scope = snackbarScope, message = addressCopiedMessage)
        },
        onCopyPhone = { phone: String ->
            clipboardManager.setText(AnnotatedString(phone))
            snackbarHostState.showSingleSnackbar(scope = snackbarScope, message = phoneCopiedMessage)
        },
        modifier = modifier,
    )
}

@Composable
private fun PlaceDetailScreen(
    placeName: String,
    placeDetailUiState: PlaceDetailUiState,
    onBackClick: () -> Unit,
    onCopyAddress: (String) -> Unit = {},
    onCopyPhone: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            DefaultToolbar(
                title = placeName,
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
                    .fillMaxSize(),
        ) {
            when (val state = placeDetailUiState) {
                PlaceDetailUiState.Loading ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                is PlaceDetailUiState.Success ->
                    PlaceDetailContent(
                        placeDetail = state.detail,
                        onCopyAddress = onCopyAddress,
                        onCopyPhone = onCopyPhone,
                    )

                is PlaceDetailUiState.Error ->
                    PlaceDetailMessage(message = stringResource(Res.string.place_list_error))
            }
        }
    }
}

@Composable
private fun PlaceDetailMessage(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, style = PretendardMedium16, color = Gray600)
    }
}

@Composable
private fun PlaceDetailContent(
    placeDetail: PlaceDetailUiModel,
    onCopyAddress: (String) -> Unit,
    onCopyPhone: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050)
                .verticalScroll(rememberScrollState()),
    ) {
        PlaceHeroImage(placeDetail = placeDetail)
        PlaceDetailHeader(placeDetail = placeDetail)
        if (!placeDetail.overview.isNullOrBlank()) {
            PlaceDescription(description = placeDetail.overview)
        }
        PlaceLocationSection(placeDetail = placeDetail, onCopyAddress = onCopyAddress)
        PlaceInfoCards(placeDetail = placeDetail, onCopyPhone = onCopyPhone)
        if (placeDetail.rows.isNotEmpty() || !placeDetail.homepage.isNullOrBlank()) {
            PlaceExtraInfoSection(placeDetail = placeDetail)
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
                .background(placeDetail.category.categoryThumbnailColor),
    ) {
        if (placeDetail.imageUrl != null) {
            AsyncImage(
                model = placeDetail.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                painter = painterResource(Res.drawable.ic_globe_location_pin),
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(64.dp).align(Alignment.Center),
            )
        }
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
                .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 20.dp),
    ) {
        PlaceCategoryLabel(label = stringResource(placeDetail.category.labelResource))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = placeDetail.name,
            style = PretendardBold.copy(fontSize = 24.sp, lineHeight = 28.sp),
            color = Gray900,
        )
        if (placeDetail.distanceMeters != null) {
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
                    text = formatDistanceMeters(placeDetail.distanceMeters),
                    style = PretendardRegular12,
                    color = Gray600,
                )
            }
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
        color = Gray900,
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
    onCopyAddress: (String) -> Unit,
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
            address = placeDetail.address ?: "",
            placeName = placeDetail.name,
            latitude = placeDetail.latitude,
            longitude = placeDetail.longitude,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(198.dp)
                    .clip(RoundedCornerShape(12.dp)),
        )
        if (!placeDetail.address.isNullOrBlank()) {
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
                Text(
                    text = placeDetail.address,
                    style = PretendardMedium16,
                    color = Gray900,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .background(Gray100, RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = rememberNoRippleInteractionSource(),
                                indication = null,
                                onClick = { onCopyAddress(placeDetail.address) },
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
}

@Composable
private fun PlaceInfoCards(
    placeDetail: PlaceDetailUiModel,
    onCopyPhone: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (placeDetail.businessHours.isNullOrBlank() && placeDetail.tel.isNullOrBlank()) return

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(White)
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
    ) {
        if (!placeDetail.businessHours.isNullOrBlank()) {
            PlaceInfoCard(
                icon = Res.drawable.ic_clock,
                title = stringResource(Res.string.place_detail_business_hours),
                value = placeDetail.businessHours,
                modifier = Modifier.weight(1f),
            )
        }
        if (!placeDetail.businessHours.isNullOrBlank() && !placeDetail.tel.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(12.dp))
        }
        if (!placeDetail.tel.isNullOrBlank()) {
            PlaceInfoCard(
                icon = Res.drawable.ic_phone,
                title = stringResource(Res.string.place_detail_phone_number),
                value = placeDetail.tel,
                onClick = { onCopyPhone(placeDetail.tel) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PlaceInfoCard(
    icon: org.jetbrains.compose.resources.DrawableResource,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .background(White, RoundedCornerShape(12.dp))
                .border(1.dp, Gray300, RoundedCornerShape(12.dp))
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = rememberNoRippleInteractionSource(),
                            indication = null,
                            onClick = onClick,
                        )
                    } else {
                        Modifier
                    },
                ).padding(20.dp),
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
private fun PlaceExtraInfoSection(
    placeDetail: PlaceDetailUiModel,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(White)
                .padding(20.dp),
    ) {
        Text(
            text = stringResource(Res.string.place_detail_extra_info_title),
            style = PretendardBold20,
            color = Gray900,
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (!placeDetail.homepage.isNullOrBlank()) {
            PlaceExtraInfoRow(
                label = stringResource(Res.string.place_detail_homepage_title),
                value = placeDetail.homepage,
                onClick = { uriHandler.openUri(placeDetail.homepage) },
            )
        }

        placeDetail.rows.forEach { row: PlaceDetailRow ->
            PlaceExtraInfoRow(label = stringResource(row.labelRes), value = row.value)
        }
    }
}

@Composable
private fun PlaceExtraInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = PretendardMedium16,
            color = Gray600,
            modifier = Modifier.width(110.dp),
        )
        Text(
            text = value,
            style = if (onClick != null) PretendardRegular16.copy(textDecoration = TextDecoration.Underline) else PretendardRegular16,
            color = if (onClick != null) Primary500 else Gray900,
            modifier =
                Modifier
                    .weight(1f)
                    .then(
                        if (onClick != null) {
                            Modifier.clickable(
                                interactionSource = rememberNoRippleInteractionSource(),
                                indication = null,
                                onClick = onClick,
                            )
                        } else {
                            Modifier
                        },
                    ),
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

private val PLACE_DETAIL_UI_MODEL: PlaceDetailUiModel =
    PlaceDetailUiModel(
        id = 1L,
        category = PlaceCategory.FOOD,
        name = "잠실 원조 순대국밥",
        address = "서울 송파구 올림픽로 25",
        latitude = 37.5122,
        longitude = 127.0715,
        tel = "02-123-4567",
        imageUrl = "https://picsum.photos/seed/place-detail-preview/800/600",
        overview = "경기 전후로 팬들이 가장 많이 찾는 순대국밥 명소입니다. 바삭한 튀김과 깊은 소스 맛으로 이미 소문난 맛집입니다.",
        homepage = "http://example-restaurant.com",
        distanceMeters = 320,
        businessHours = "11:00~22:00",
        rows =
            listOf(
                PlaceDetailRow(labelRes = Res.string.place_detail_field_firstmenu, value = "순대국밥"),
                PlaceDetailRow(labelRes = Res.string.place_detail_field_smoking, value = "불가능"),
                PlaceDetailRow(labelRes = Res.string.place_detail_field_packing, value = "가능"),
            ),
    )

private val PLACE_DETAIL_UI_MODEL_MINIMAL: PlaceDetailUiModel =
    PlaceDetailUiModel(
        id = 2L,
        category = PlaceCategory.TOUR,
        name = "롯데월드타워",
        address = null,
        latitude = 37.5125,
        longitude = 127.1025,
        tel = null,
        imageUrl = null,
        overview = null,
        homepage = null,
        distanceMeters = null,
        businessHours = null,
        rows = emptyList(),
    )

@Preview("플레이스 상세 화면")
@Composable
private fun PlaceDetailScreenPreview() {
    PlaceDetailScreen(
        placeName = PLACE_DETAIL_UI_MODEL.name,
        placeDetailUiState = PlaceDetailUiState.Success(PLACE_DETAIL_UI_MODEL),
        onBackClick = {},
    )
}

@Preview("플레이스 상세 화면 - 최소 정보")
@Composable
private fun PlaceDetailScreenMinimalPreview() {
    PlaceDetailScreen(
        placeName = PLACE_DETAIL_UI_MODEL_MINIMAL.name,
        placeDetailUiState = PlaceDetailUiState.Success(PLACE_DETAIL_UI_MODEL_MINIMAL),
        onBackClick = {},
    )
}

@Preview("플레이스 상세 화면 - 로딩")
@Composable
private fun PlaceDetailScreenLoadingPreview() {
    PlaceDetailScreen(
        placeName = "잠실 원조 순대국밥",
        placeDetailUiState = PlaceDetailUiState.Loading,
        onBackClick = {},
    )
}
