package com.yagubogu.data.dto.response.place

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FoodDetailDto(
    @SerialName("seat")
    val seat: String? = null,
    @SerialName("kidsfacility")
    val kidsfacility: String? = null,
    @SerialName("firstmenu")
    val firstmenu: String? = null,
    @SerialName("treatmenu")
    val treatmenu: String? = null,
    @SerialName("smoking")
    val smoking: String? = null,
    @SerialName("packing")
    val packing: String? = null,
    @SerialName("infocenterfood")
    val infocenterfood: String? = null,
    @SerialName("scalefood")
    val scalefood: String? = null,
    @SerialName("parkingfood")
    val parkingfood: String? = null,
    @SerialName("opendatefood")
    val opendatefood: String? = null,
    @SerialName("opentimefood")
    val opentimefood: String? = null,
    @SerialName("restdatefood")
    val restdatefood: String? = null,
    @SerialName("discountinfofood")
    val discountinfofood: String? = null,
    @SerialName("chkcreditcardfood")
    val chkcreditcardfood: String? = null,
    @SerialName("reservationfood")
    val reservationfood: String? = null,
    @SerialName("lcnsno")
    val lcnsno: String? = null,
)
