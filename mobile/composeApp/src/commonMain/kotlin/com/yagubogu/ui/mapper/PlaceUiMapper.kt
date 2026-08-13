package com.yagubogu.ui.mapper

import com.yagubogu.data.dto.response.place.AttractionDetailDto
import com.yagubogu.data.dto.response.place.FoodDetailDto
import com.yagubogu.data.dto.response.place.LodgingDetailDto
import com.yagubogu.data.dto.response.place.PerformanceDetailDto
import com.yagubogu.data.dto.response.place.PlaceCategoryDto
import com.yagubogu.data.dto.response.place.PlaceDetailResponse
import com.yagubogu.data.dto.response.place.PlaceDto
import com.yagubogu.ui.place.model.PlaceCategory
import com.yagubogu.ui.place.model.PlaceDetailRow
import com.yagubogu.ui.place.model.PlaceDetailUiModel
import com.yagubogu.ui.place.model.PlaceItem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.place_detail_field_accomcount
import yagubogu.composeapp.generated.resources.place_detail_field_accomcountlodging
import yagubogu.composeapp.generated.resources.place_detail_field_agelimit
import yagubogu.composeapp.generated.resources.place_detail_field_barbecue
import yagubogu.composeapp.generated.resources.place_detail_field_beauty
import yagubogu.composeapp.generated.resources.place_detail_field_beverage
import yagubogu.composeapp.generated.resources.place_detail_field_bicycle
import yagubogu.composeapp.generated.resources.place_detail_field_bookingplace
import yagubogu.composeapp.generated.resources.place_detail_field_campfire
import yagubogu.composeapp.generated.resources.place_detail_field_chkbabycarriage
import yagubogu.composeapp.generated.resources.place_detail_field_chkcooking
import yagubogu.composeapp.generated.resources.place_detail_field_chkcreditcard
import yagubogu.composeapp.generated.resources.place_detail_field_chkcreditcardfood
import yagubogu.composeapp.generated.resources.place_detail_field_chkpet
import yagubogu.composeapp.generated.resources.place_detail_field_discountinfofestival
import yagubogu.composeapp.generated.resources.place_detail_field_discountinfofood
import yagubogu.composeapp.generated.resources.place_detail_field_eventenddate
import yagubogu.composeapp.generated.resources.place_detail_field_eventhomepage
import yagubogu.composeapp.generated.resources.place_detail_field_eventplace
import yagubogu.composeapp.generated.resources.place_detail_field_eventstartdate
import yagubogu.composeapp.generated.resources.place_detail_field_expagerange
import yagubogu.composeapp.generated.resources.place_detail_field_expguide
import yagubogu.composeapp.generated.resources.place_detail_field_festivalgrade
import yagubogu.composeapp.generated.resources.place_detail_field_festivaltype
import yagubogu.composeapp.generated.resources.place_detail_field_firstmenu
import yagubogu.composeapp.generated.resources.place_detail_field_fitness
import yagubogu.composeapp.generated.resources.place_detail_field_foodplace
import yagubogu.composeapp.generated.resources.place_detail_field_heritage1
import yagubogu.composeapp.generated.resources.place_detail_field_heritage2
import yagubogu.composeapp.generated.resources.place_detail_field_heritage3
import yagubogu.composeapp.generated.resources.place_detail_field_infocenter
import yagubogu.composeapp.generated.resources.place_detail_field_infocenterfood
import yagubogu.composeapp.generated.resources.place_detail_field_infocenterlodging
import yagubogu.composeapp.generated.resources.place_detail_field_karaoke
import yagubogu.composeapp.generated.resources.place_detail_field_kidsfacility
import yagubogu.composeapp.generated.resources.place_detail_field_lcnsno
import yagubogu.composeapp.generated.resources.place_detail_field_opendate
import yagubogu.composeapp.generated.resources.place_detail_field_opendatefood
import yagubogu.composeapp.generated.resources.place_detail_field_packing
import yagubogu.composeapp.generated.resources.place_detail_field_parking
import yagubogu.composeapp.generated.resources.place_detail_field_parkingfood
import yagubogu.composeapp.generated.resources.place_detail_field_parkinglodging
import yagubogu.composeapp.generated.resources.place_detail_field_pickup
import yagubogu.composeapp.generated.resources.place_detail_field_placeinfo
import yagubogu.composeapp.generated.resources.place_detail_field_program
import yagubogu.composeapp.generated.resources.place_detail_field_progresstype
import yagubogu.composeapp.generated.resources.place_detail_field_publicbath
import yagubogu.composeapp.generated.resources.place_detail_field_publicpc
import yagubogu.composeapp.generated.resources.place_detail_field_refundregulation
import yagubogu.composeapp.generated.resources.place_detail_field_reservationfood
import yagubogu.composeapp.generated.resources.place_detail_field_reservationlodging
import yagubogu.composeapp.generated.resources.place_detail_field_reservationurl
import yagubogu.composeapp.generated.resources.place_detail_field_restdate
import yagubogu.composeapp.generated.resources.place_detail_field_restdatefood
import yagubogu.composeapp.generated.resources.place_detail_field_roomcount
import yagubogu.composeapp.generated.resources.place_detail_field_roomtype
import yagubogu.composeapp.generated.resources.place_detail_field_sauna
import yagubogu.composeapp.generated.resources.place_detail_field_scalefood
import yagubogu.composeapp.generated.resources.place_detail_field_scalelodging
import yagubogu.composeapp.generated.resources.place_detail_field_seat
import yagubogu.composeapp.generated.resources.place_detail_field_seminar
import yagubogu.composeapp.generated.resources.place_detail_field_smoking
import yagubogu.composeapp.generated.resources.place_detail_field_spendtimefestival
import yagubogu.composeapp.generated.resources.place_detail_field_sponsor1
import yagubogu.composeapp.generated.resources.place_detail_field_sponsor1tel
import yagubogu.composeapp.generated.resources.place_detail_field_sponsor2
import yagubogu.composeapp.generated.resources.place_detail_field_sponsor2tel
import yagubogu.composeapp.generated.resources.place_detail_field_sports
import yagubogu.composeapp.generated.resources.place_detail_field_subevent
import yagubogu.composeapp.generated.resources.place_detail_field_subfacility
import yagubogu.composeapp.generated.resources.place_detail_field_treatmenu
import yagubogu.composeapp.generated.resources.place_detail_field_useseason
import yagubogu.composeapp.generated.resources.place_detail_field_usetimefestival

private val placeDetailJson = Json { ignoreUnknownKeys = true }

fun PlaceCategory.toApiCategory(): PlaceCategoryDto =
    when (this) {
        PlaceCategory.FOOD -> PlaceCategoryDto.RESTAURANT
        PlaceCategory.CAFE -> PlaceCategoryDto.CAFE
        PlaceCategory.STAY -> PlaceCategoryDto.LODGING
        PlaceCategory.TOUR -> PlaceCategoryDto.ATTRACTION
        PlaceCategory.SHOW -> PlaceCategoryDto.PERFORMANCE
    }

fun PlaceCategoryDto.toUiCategory(): PlaceCategory =
    when (this) {
        PlaceCategoryDto.RESTAURANT -> PlaceCategory.FOOD
        PlaceCategoryDto.CAFE -> PlaceCategory.CAFE
        PlaceCategoryDto.LODGING -> PlaceCategory.STAY
        PlaceCategoryDto.ATTRACTION -> PlaceCategory.TOUR
        PlaceCategoryDto.PERFORMANCE -> PlaceCategory.SHOW
    }

private fun formatDistanceMeters(meters: Int?): String = if (meters == null) "" else "도보 ${meters}m"

fun PlaceDto.toUiModel(): PlaceItem =
    PlaceItem(
        id = id,
        category = category.toUiCategory(),
        name = title,
        distance = formatDistanceMeters(distance),
        imageUrl = imageUrl,
    )

fun PlaceDetailResponse.toUiModel(distanceMeters: Int?): PlaceDetailUiModel {
    val decoded: DecodedPlaceDetail = decodePlaceDetail(category, detail)

    return PlaceDetailUiModel(
        id = id,
        category = category.toUiCategory(),
        name = title,
        address = address,
        latitude = mapY,
        longitude = mapX,
        tel = tel,
        imageUrl = imageUrl,
        overview = overview,
        homepage = homepage,
        distanceMeters = distanceMeters,
        businessHours = decoded.businessHours,
        rows = decoded.rows,
    )
}

private data class DecodedPlaceDetail(
    val businessHours: String?,
    val rows: List<PlaceDetailRow>,
)

private fun decodePlaceDetail(
    category: PlaceCategoryDto,
    detail: JsonObject?,
): DecodedPlaceDetail {
    if (detail == null) return DecodedPlaceDetail(businessHours = null, rows = emptyList())

    return when (category) {
        PlaceCategoryDto.RESTAURANT, PlaceCategoryDto.CAFE ->
            placeDetailJson.decodeFromJsonElement(FoodDetailDto.serializer(), detail).toDecoded()
        PlaceCategoryDto.ATTRACTION ->
            placeDetailJson.decodeFromJsonElement(AttractionDetailDto.serializer(), detail).toDecoded()
        PlaceCategoryDto.LODGING ->
            placeDetailJson.decodeFromJsonElement(LodgingDetailDto.serializer(), detail).toDecoded()
        PlaceCategoryDto.PERFORMANCE ->
            placeDetailJson.decodeFromJsonElement(PerformanceDetailDto.serializer(), detail).toDecoded()
    }
}

private fun row(
    labelRes: org.jetbrains.compose.resources.StringResource,
    value: String?,
): PlaceDetailRow? = value?.takeIf { it.isNotBlank() }?.let { PlaceDetailRow(labelRes, it) }

private fun FoodDetailDto.toDecoded(): DecodedPlaceDetail =
    DecodedPlaceDetail(
        businessHours = opentimefood,
        rows =
            listOfNotNull(
                row(Res.string.place_detail_field_seat, seat),
                row(Res.string.place_detail_field_kidsfacility, kidsfacility),
                row(Res.string.place_detail_field_firstmenu, firstmenu),
                row(Res.string.place_detail_field_treatmenu, treatmenu),
                row(Res.string.place_detail_field_smoking, smoking),
                row(Res.string.place_detail_field_packing, packing),
                row(Res.string.place_detail_field_infocenterfood, infocenterfood),
                row(Res.string.place_detail_field_scalefood, scalefood),
                row(Res.string.place_detail_field_parkingfood, parkingfood),
                row(Res.string.place_detail_field_opendatefood, opendatefood),
                row(Res.string.place_detail_field_restdatefood, restdatefood),
                row(Res.string.place_detail_field_discountinfofood, discountinfofood),
                row(Res.string.place_detail_field_chkcreditcardfood, chkcreditcardfood),
                row(Res.string.place_detail_field_reservationfood, reservationfood),
                row(Res.string.place_detail_field_lcnsno, lcnsno),
            ),
    )

private fun AttractionDetailDto.toDecoded(): DecodedPlaceDetail =
    DecodedPlaceDetail(
        businessHours = usetime,
        rows =
            listOfNotNull(
                row(Res.string.place_detail_field_heritage1, heritage1),
                row(Res.string.place_detail_field_heritage2, heritage2),
                row(Res.string.place_detail_field_heritage3, heritage3),
                row(Res.string.place_detail_field_infocenter, infocenter),
                row(Res.string.place_detail_field_opendate, opendate),
                row(Res.string.place_detail_field_restdate, restdate),
                row(Res.string.place_detail_field_expguide, expguide),
                row(Res.string.place_detail_field_expagerange, expagerange),
                row(Res.string.place_detail_field_accomcount, accomcount),
                row(Res.string.place_detail_field_useseason, useseason),
                row(Res.string.place_detail_field_parking, parking),
                row(Res.string.place_detail_field_chkbabycarriage, chkbabycarriage),
                row(Res.string.place_detail_field_chkpet, chkpet),
                row(Res.string.place_detail_field_chkcreditcard, chkcreditcard),
            ),
    )

private fun LodgingDetailDto.toDecoded(): DecodedPlaceDetail {
    val businessHours: String? =
        when {
            checkintime != null && checkouttime != null -> "$checkintime ~ $checkouttime"
            checkintime != null -> checkintime
            checkouttime != null -> checkouttime
            else -> null
        }
    return DecodedPlaceDetail(
        businessHours = businessHours,
        rows =
            listOfNotNull(
                row(Res.string.place_detail_field_roomcount, roomcount),
                row(Res.string.place_detail_field_roomtype, roomtype),
                row(Res.string.place_detail_field_refundregulation, refundregulation),
                row(Res.string.place_detail_field_chkcooking, chkcooking),
                row(Res.string.place_detail_field_seminar, seminar),
                row(Res.string.place_detail_field_sports, sports),
                row(Res.string.place_detail_field_sauna, sauna),
                row(Res.string.place_detail_field_beauty, beauty),
                row(Res.string.place_detail_field_beverage, beverage),
                row(Res.string.place_detail_field_karaoke, karaoke),
                row(Res.string.place_detail_field_barbecue, barbecue),
                row(Res.string.place_detail_field_campfire, campfire),
                row(Res.string.place_detail_field_bicycle, bicycle),
                row(Res.string.place_detail_field_fitness, fitness),
                row(Res.string.place_detail_field_publicpc, publicpc),
                row(Res.string.place_detail_field_publicbath, publicbath),
                row(Res.string.place_detail_field_subfacility, subfacility),
                row(Res.string.place_detail_field_foodplace, foodplace),
                row(Res.string.place_detail_field_reservationurl, reservationurl),
                row(Res.string.place_detail_field_pickup, pickup),
                row(Res.string.place_detail_field_infocenterlodging, infocenterlodging),
                row(Res.string.place_detail_field_parkinglodging, parkinglodging),
                row(Res.string.place_detail_field_reservationlodging, reservationlodging),
                row(Res.string.place_detail_field_scalelodging, scalelodging),
                row(Res.string.place_detail_field_accomcountlodging, accomcountlodging),
            ),
    )
}

private fun PerformanceDetailDto.toDecoded(): DecodedPlaceDetail =
    DecodedPlaceDetail(
        businessHours = playtime,
        rows =
            listOfNotNull(
                row(Res.string.place_detail_field_sponsor1, sponsor1),
                row(Res.string.place_detail_field_sponsor1tel, sponsor1tel),
                row(Res.string.place_detail_field_sponsor2, sponsor2),
                row(Res.string.place_detail_field_sponsor2tel, sponsor2tel),
                row(Res.string.place_detail_field_eventstartdate, eventstartdate),
                row(Res.string.place_detail_field_eventenddate, eventenddate),
                row(Res.string.place_detail_field_eventplace, eventplace),
                row(Res.string.place_detail_field_eventhomepage, eventhomepage),
                row(Res.string.place_detail_field_agelimit, agelimit),
                row(Res.string.place_detail_field_bookingplace, bookingplace),
                row(Res.string.place_detail_field_placeinfo, placeinfo),
                row(Res.string.place_detail_field_subevent, subevent),
                row(Res.string.place_detail_field_program, program),
                row(Res.string.place_detail_field_usetimefestival, usetimefestival),
                row(Res.string.place_detail_field_discountinfofestival, discountinfofestival),
                row(Res.string.place_detail_field_spendtimefestival, spendtimefestival),
                row(Res.string.place_detail_field_festivalgrade, festivalgrade),
                row(Res.string.place_detail_field_progresstype, progresstype),
                row(Res.string.place_detail_field_festivaltype, festivaltype),
            ),
    )
