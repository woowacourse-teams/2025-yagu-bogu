package com.yagubogu.restaurant.dto.v1;

import java.util.List;

public record RestaurantsResponse(
        Long stadiumId,
        List<RestaurantResponse> restaurants
) {}
