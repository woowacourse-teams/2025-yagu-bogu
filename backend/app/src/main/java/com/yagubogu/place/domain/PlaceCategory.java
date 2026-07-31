package com.yagubogu.place.domain;

public enum PlaceCategory {

    ATTRACTION(12, null),
    PERFORMANCE(15, null),
    LODGING(32, null),
    RESTAURANT(39, null),
    // 카페는 KTO API에 별도 contentTypeId가 없어 음식점(39)을 cat3(서비스분류코드)로 필터링한다.
    CAFE(39, "A05020900");

    private final int contentTypeId;
    private final String cat3;

    PlaceCategory(int contentTypeId, String cat3) {
        this.contentTypeId = contentTypeId;
        this.cat3 = cat3;
    }

    public int getContentTypeId() {
        return contentTypeId;
    }

    public String getCat3() {
        return cat3;
    }
}
