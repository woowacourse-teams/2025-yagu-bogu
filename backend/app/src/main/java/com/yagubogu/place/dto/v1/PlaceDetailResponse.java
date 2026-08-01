package com.yagubogu.place.dto.v1;

import com.yagubogu.place.domain.Place;
import com.yagubogu.place.domain.PlaceCategory;
import com.yagubogu.place.dto.v1.detail.PlaceDetail;

public record PlaceDetailResponse(
        Long id,
        PlaceCategory category,
        String title,
        String address,
        Double mapX,
        Double mapY,
        String tel,
        String imageUrl,
        String overview,
        String homepage,
        PlaceDetail detail
) {
    public static PlaceDetailResponse from(Place place, String overview, String homepage, PlaceDetail detail) {
        return new PlaceDetailResponse(
                place.getId(),
                place.getCategory(),
                place.getTitle(),
                place.getAddress(),
                place.getMapX(),
                place.getMapY(),
                place.getTel(),
                place.getImageUrl(),
                overview,
                homepage,
                detail
        );
    }
}
