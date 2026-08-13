package com.yagubogu.place.dto.v1;

import com.yagubogu.place.domain.Place;
import com.yagubogu.place.domain.PlaceCategory;

public record PlaceResponse(
        Long id,
        PlaceCategory category,
        String title,
        String address,
        Double mapX,
        Double mapY,
        Integer distance,
        String tel,
        String imageUrl
) {
    public static PlaceResponse from(Place place) {
        return new PlaceResponse(
                place.getId(),
                place.getCategory(),
                place.getTitle(),
                place.getAddress(),
                place.getMapX(),
                place.getMapY(),
                place.getDistance(),
                place.getTel(),
                place.getImageUrl()
        );
    }
}
