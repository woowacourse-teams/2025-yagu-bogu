package com.yagubogu.place.dto.v1.detail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Tour API detailIntro2 (contentTypeId=15, 축제공연행사) 응답 필드 전체를 담는다.
 * contentid/contenttypeid는 PlaceDetailResponse의 id/category와 중복이라 제외한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PerformanceDetail(
        String sponsor1,
        String sponsor1tel,
        String sponsor2,
        String sponsor2tel,
        String eventstartdate,
        String eventenddate,
        String playtime,
        String eventplace,
        String eventhomepage,
        String agelimit,
        String bookingplace,
        String placeinfo,
        String subevent,
        String program,
        String usetimefestival,
        String discountinfofestival,
        String spendtimefestival,
        String festivalgrade,
        String progresstype,
        String festivaltype
) implements PlaceDetail {
}
