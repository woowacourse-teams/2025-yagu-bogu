package com.yagubogu.data.dto.response.place

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LodgingDetailDto(
    @SerialName("roomcount")
    val roomcount: String? = null,
    @SerialName("roomtype")
    val roomtype: String? = null,
    @SerialName("refundregulation")
    val refundregulation: String? = null,
    @SerialName("checkintime")
    val checkintime: String? = null,
    @SerialName("checkouttime")
    val checkouttime: String? = null,
    @SerialName("chkcooking")
    val chkcooking: String? = null,
    @SerialName("seminar")
    val seminar: String? = null,
    @SerialName("sports")
    val sports: String? = null,
    @SerialName("sauna")
    val sauna: String? = null,
    @SerialName("beauty")
    val beauty: String? = null,
    @SerialName("beverage")
    val beverage: String? = null,
    @SerialName("karaoke")
    val karaoke: String? = null,
    @SerialName("barbecue")
    val barbecue: String? = null,
    @SerialName("campfire")
    val campfire: String? = null,
    @SerialName("bicycle")
    val bicycle: String? = null,
    @SerialName("fitness")
    val fitness: String? = null,
    @SerialName("publicpc")
    val publicpc: String? = null,
    @SerialName("publicbath")
    val publicbath: String? = null,
    @SerialName("subfacility")
    val subfacility: String? = null,
    @SerialName("foodplace")
    val foodplace: String? = null,
    @SerialName("reservationurl")
    val reservationurl: String? = null,
    @SerialName("pickup")
    val pickup: String? = null,
    @SerialName("infocenterlodging")
    val infocenterlodging: String? = null,
    @SerialName("parkinglodging")
    val parkinglodging: String? = null,
    @SerialName("reservationlodging")
    val reservationlodging: String? = null,
    @SerialName("scalelodging")
    val scalelodging: String? = null,
    @SerialName("accomcountlodging")
    val accomcountlodging: String? = null,
)
