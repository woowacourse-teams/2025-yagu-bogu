package com.yagubogu.data.dto.response.place

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttractionDetailDto(
    @SerialName("heritage1")
    val heritage1: String? = null,
    @SerialName("heritage2")
    val heritage2: String? = null,
    @SerialName("heritage3")
    val heritage3: String? = null,
    @SerialName("infocenter")
    val infocenter: String? = null,
    @SerialName("opendate")
    val opendate: String? = null,
    @SerialName("restdate")
    val restdate: String? = null,
    @SerialName("expguide")
    val expguide: String? = null,
    @SerialName("expagerange")
    val expagerange: String? = null,
    @SerialName("accomcount")
    val accomcount: String? = null,
    @SerialName("useseason")
    val useseason: String? = null,
    @SerialName("usetime")
    val usetime: String? = null,
    @SerialName("parking")
    val parking: String? = null,
    @SerialName("chkbabycarriage")
    val chkbabycarriage: String? = null,
    @SerialName("chkpet")
    val chkpet: String? = null,
    @SerialName("chkcreditcard")
    val chkcreditcard: String? = null,
)
