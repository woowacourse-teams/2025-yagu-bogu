package com.yagubogu.place.dto.v1.detail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Tour API detailIntro2 (contentTypeId=12, 관광지) 응답 필드 전체를 담는다.
 * contentid/contenttypeid는 PlaceDetailResponse의 id/category와 중복이라 제외한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AttractionDetail(
        String heritage1,
        String heritage2,
        String heritage3,
        String infocenter,
        String opendate,
        String restdate,
        String expguide,
        String expagerange,
        String accomcount,
        String useseason,
        String usetime,
        String parking,
        String chkbabycarriage,
        String chkpet,
        String chkcreditcard
) implements PlaceDetail {
}
