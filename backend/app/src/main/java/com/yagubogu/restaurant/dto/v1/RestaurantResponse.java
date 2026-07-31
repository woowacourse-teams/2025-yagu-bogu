package com.yagubogu.restaurant.dto.v1;

import com.yagubogu.restaurant.domain.Restaurant;

public record RestaurantResponse(
        Long id,
        String title,
        String address,
        Double mapX,
        Double mapY,
        Integer distance,
        String tel,
        String imageUrl
) {
    public static RestaurantResponse from(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getTitle(),
                restaurant.getAddress(),
                restaurant.getMapX(),
                restaurant.getMapY(),
                restaurant.getDistance(),
                restaurant.getTel(),
                restaurant.getImageUrl()
        );
    }
}
