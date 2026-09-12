package com.yagubogu.data.dto.response.place

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PerformanceDetailDto(
    @SerialName("sponsor1")
    val sponsor1: String? = null,
    @SerialName("sponsor1tel")
    val sponsor1tel: String? = null,
    @SerialName("sponsor2")
    val sponsor2: String? = null,
    @SerialName("sponsor2tel")
    val sponsor2tel: String? = null,
    @SerialName("eventstartdate")
    val eventstartdate: String? = null,
    @SerialName("eventenddate")
    val eventenddate: String? = null,
    @SerialName("playtime")
    val playtime: String? = null,
    @SerialName("eventplace")
    val eventplace: String? = null,
    @SerialName("eventhomepage")
    val eventhomepage: String? = null,
    @SerialName("agelimit")
    val agelimit: String? = null,
    @SerialName("bookingplace")
    val bookingplace: String? = null,
    @SerialName("placeinfo")
    val placeinfo: String? = null,
    @SerialName("subevent")
    val subevent: String? = null,
    @SerialName("program")
    val program: String? = null,
    @SerialName("usetimefestival")
    val usetimefestival: String? = null,
    @SerialName("discountinfofestival")
    val discountinfofestival: String? = null,
    @SerialName("spendtimefestival")
    val spendtimefestival: String? = null,
    @SerialName("festivalgrade")
    val festivalgrade: String? = null,
    @SerialName("progresstype")
    val progresstype: String? = null,
    @SerialName("festivaltype")
    val festivaltype: String? = null,
)
