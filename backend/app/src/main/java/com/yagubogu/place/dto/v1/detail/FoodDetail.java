package com.yagubogu.place.dto.v1.detail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Tour API detailIntro2 (contentTypeId=39, 음식점/카페) 응답 필드 전체를 담는다.
 * contentid/contenttypeid는 PlaceDetailResponse의 id/category와 중복이라 제외한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FoodDetail(
        String seat,
        String kidsfacility,
        String firstmenu,
        String treatmenu,
        String smoking,
        String packing,
        String infocenterfood,
        String scalefood,
        String parkingfood,
        String opendatefood,
        String opentimefood,
        String restdatefood,
        String discountinfofood,
        String chkcreditcardfood,
        String reservationfood,
        String lcnsno
) implements PlaceDetail {
}
