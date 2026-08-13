package com.yagubogu.place.dto.v1.detail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Tour API detailIntro2 (contentTypeId=32, 숙박) 응답 필드 전체를 담는다.
 * contentid/contenttypeid는 PlaceDetailResponse의 id/category와 중복이라 제외한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LodgingDetail(
        String roomcount,
        String roomtype,
        String refundregulation,
        String checkintime,
        String checkouttime,
        String chkcooking,
        String seminar,
        String sports,
        String sauna,
        String beauty,
        String beverage,
        String karaoke,
        String barbecue,
        String campfire,
        String bicycle,
        String fitness,
        String publicpc,
        String publicbath,
        String subfacility,
        String foodplace,
        String reservationurl,
        String pickup,
        String infocenterlodging,
        String parkinglodging,
        String reservationlodging,
        String scalelodging,
        String accomcountlodging
) implements PlaceDetail {
}
