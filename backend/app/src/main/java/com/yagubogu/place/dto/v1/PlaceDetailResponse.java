package com.yagubogu.place.dto.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.yagubogu.place.domain.Place;
import com.yagubogu.place.domain.PlaceCategory;

public record PlaceDetailResponse(
        Long id,
        PlaceCategory category,
        String title,
        String address,
        Double mapX,
        Double mapY,
        String tel,
        String imageUrl,
        JsonNode detail
) {
    public static PlaceDetailResponse from(Place place, JsonNode detail) {
        return new PlaceDetailResponse(
                place.getId(),
                place.getCategory(),
                place.getTitle(),
                place.getAddress(),
                place.getMapX(),
                place.getMapY(),
                place.getTel(),
                place.getImageUrl(),
                detail
        );
    }
}
