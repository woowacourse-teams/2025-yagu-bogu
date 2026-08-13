package com.yagubogu.data.datasource.place

import com.yagubogu.data.dto.response.place.PlaceCategoryDto
import com.yagubogu.data.dto.response.place.PlaceDetailResponse
import com.yagubogu.data.dto.response.place.PlaceDto
import com.yagubogu.data.dto.response.place.PlacesResponse
import com.yagubogu.data.util.ApiException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// 백엔드 /api/v1/places API가 아직 배포되지 않아 화면 테스트용으로 임시로 둔 목 데이터 소스.
// DataSourceModule.kt의 USE_PLACE_MOCK_DATA 플래그로 PlaceRemoteDataSource 대신 바인딩된다.
// 실제 API 배포 후 플래그를 false로 되돌리거나 이 파일과 함께 제거한다.
class PlaceMockDataSource : PlaceDataSource {
    override suspend fun getPlaces(
        stadiumId: Long,
        category: PlaceCategoryDto,
    ): Result<PlacesResponse> =
        Result.success(
            PlacesResponse(
                stadiumId = stadiumId,
                category = category,
                places = MOCK_PLACES.filter { it.category == category },
            ),
        )

    override suspend fun getPlaceDetail(id: Long): Result<PlaceDetailResponse> {
        val response: PlaceDetailResponse =
            MOCK_DETAILS[id] ?: return Result.failure(ApiException.NotFound("Place is not found"))
        return Result.success(response)
    }
}

private val MOCK_PLACES: List<PlaceDto> =
    listOf(
        PlaceDto(
            id = 1,
            category = PlaceCategoryDto.RESTAURANT,
            title = "잠실 원조 순대국밥",
            address = "서울 송파구 올림픽로 25",
            mapX = 127.0715,
            mapY = 37.5122,
            distance = 320,
            tel = "02-123-4567",
            imageUrl = null,
        ),
        PlaceDto(
            id = 2,
            category = PlaceCategoryDto.RESTAURANT,
            title = "홈런 한우구이",
            address = "서울 송파구 올림픽로 32길 8",
            mapX = 127.0769,
            mapY = 37.5145,
            distance = 680,
            tel = "02-333-4567",
            imageUrl = "https://picsum.photos/seed/place-food/400/300",
        ),
        PlaceDto(
            id = 3,
            category = PlaceCategoryDto.CAFE,
            title = "카페 베이스런",
            address = "서울 송파구 올림픽로 45",
            mapX = 127.0739,
            mapY = 37.5161,
            distance = 810,
            tel = "02-444-4567",
            imageUrl = "https://picsum.photos/seed/place-cafe/400/300",
        ),
        PlaceDto(
            id = 4,
            category = PlaceCategoryDto.LODGING,
            title = "롯데호텔월드",
            address = "서울 송파구 올림픽로 240",
            mapX = 127.0985,
            mapY = 37.5111,
            distance = 650,
            tel = "02-419-7000",
            imageUrl = null,
        ),
        PlaceDto(
            id = 5,
            category = PlaceCategoryDto.ATTRACTION,
            title = "롯데월드타워",
            address = "서울 송파구 올림픽로 300",
            mapX = 127.1025,
            mapY = 37.5125,
            distance = 700,
            tel = "1661-2000",
            imageUrl = "https://picsum.photos/seed/place-tour/400/300",
        ),
        PlaceDto(
            id = 6,
            category = PlaceCategoryDto.PERFORMANCE,
            title = "잠실 실내체육관 콘서트",
            address = "서울 송파구 올림픽로 25",
            mapX = 127.0722,
            mapY = 37.5153,
            distance = 450,
            tel = null,
            imageUrl = null,
        ),
    )

private val MOCK_DETAILS: Map<Long, PlaceDetailResponse> =
    listOf(
        PlaceDetailResponse(
            id = 1,
            category = PlaceCategoryDto.RESTAURANT,
            title = "잠실 원조 순대국밥",
            address = "서울 송파구 올림픽로 25",
            mapX = 127.0715,
            mapY = 37.5122,
            tel = "02-123-4567",
            imageUrl = null,
            overview = "경기 전후로 팬들이 가장 많이 찾는 순대국밥 명소입니다.",
            homepage = null,
            detail =
                buildJsonObject {
                    put("seat", "40석")
                    put("firstmenu", "순대국밥")
                    put("treatmenu", "순대국밥, 머릿고기")
                    put("smoking", "불가능")
                    put("packing", "가능")
                    put("opentimefood", "11:00~22:00")
                    put("restdatefood", "연중무휴")
                    put("chkcreditcardfood", "가능")
                    put("parkingfood", "불가능")
                },
        ),
        PlaceDetailResponse(
            id = 2,
            category = PlaceCategoryDto.RESTAURANT,
            title = "홈런 한우구이",
            address = "서울 송파구 올림픽로 32길 8",
            mapX = 127.0769,
            mapY = 37.5145,
            tel = "02-333-4567",
            imageUrl = "https://picsum.photos/seed/place-food/800/600",
            overview = "두툼한 한우구이와 든든한 식사 메뉴가 준비된 회식형 맛집입니다.",
            homepage = "http://example-restaurant.com",
            detail =
                buildJsonObject {
                    put("seat", "80석")
                    put("kidsfacility", "있음")
                    put("firstmenu", "한우 등심")
                    put("opentimefood", "12:00~23:00")
                    put("reservationfood", "전화 예약 필수")
                    put("parkingfood", "가능(발렛)")
                },
        ),
        PlaceDetailResponse(
            id = 3,
            category = PlaceCategoryDto.CAFE,
            title = "카페 베이스런",
            address = "서울 송파구 올림픽로 45",
            mapX = 127.0739,
            mapY = 37.5161,
            tel = "02-444-4567",
            imageUrl = "https://picsum.photos/seed/place-cafe/800/600",
            overview = "야구장 근처에서 잠시 쉬어가기 좋은 카페입니다.",
            homepage = null,
            detail =
                buildJsonObject {
                    put("seat", "30석")
                    put("opentimefood", "09:00~22:00")
                    put("smoking", "불가능")
                    put("packing", "가능")
                },
        ),
        PlaceDetailResponse(
            id = 4,
            category = PlaceCategoryDto.LODGING,
            title = "롯데호텔월드",
            address = "서울 송파구 올림픽로 240",
            mapX = 127.0985,
            mapY = 37.5111,
            tel = "02-419-7000",
            imageUrl = null,
            overview = "잠실 야구장과 가까운 특급 호텔입니다.",
            homepage = "http://www.lottehotel.com",
            detail =
                buildJsonObject {
                    put("roomcount", "477")
                    put("roomtype", "싱글, 더블, 스위트")
                    put("checkintime", "15:00")
                    put("checkouttime", "12:00")
                    put("chkcooking", "불가능")
                    put("sauna", "있음")
                    put("fitness", "있음")
                    put("parkinglodging", "가능")
                },
        ),
        PlaceDetailResponse(
            id = 5,
            category = PlaceCategoryDto.ATTRACTION,
            title = "롯데월드타워",
            address = "서울 송파구 올림픽로 300",
            mapX = 127.1025,
            mapY = 37.5125,
            tel = "1661-2000",
            imageUrl = "https://picsum.photos/seed/place-tour/800/600",
            overview = "서울의 랜드마크, 서울스카이 전망대가 있는 초고층 타워입니다.",
            homepage = "http://www.lwt.co.kr",
            detail =
                buildJsonObject {
                    put("infocenter", "1661-2000")
                    put("opendate", "2017-04-03")
                    put("usetime", "10:30~22:00")
                    put("parking", "가능")
                    put("chkbabycarriage", "가능")
                    put("chkpet", "불가능")
                    put("chkcreditcard", "가능")
                },
        ),
        PlaceDetailResponse(
            id = 6,
            category = PlaceCategoryDto.PERFORMANCE,
            title = "잠실 실내체육관 콘서트",
            address = "서울 송파구 올림픽로 25",
            mapX = 127.0722,
            mapY = 37.5153,
            tel = null,
            imageUrl = null,
            overview = "잠실 실내체육관에서 열리는 콘서트입니다.",
            homepage = null,
            detail =
                buildJsonObject {
                    put("eventstartdate", "20260901")
                    put("eventenddate", "20260902")
                    put("playtime", "19:00~21:00")
                    put("eventplace", "잠실 실내체육관")
                    put("agelimit", "8세 이상")
                    put("bookingplace", "인터파크")
                },
        ),
    ).associateBy { it.id }
