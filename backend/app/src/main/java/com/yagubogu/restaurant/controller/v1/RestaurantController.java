package com.yagubogu.restaurant.controller.v1;

import com.yagubogu.restaurant.dto.v1.RestaurantsResponse;
import com.yagubogu.restaurant.service.RestaurantQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RestaurantController implements RestaurantControllerInterface {

    private final RestaurantQueryService restaurantQueryService;

    @Override
    public ResponseEntity<RestaurantsResponse> getRestaurantsByStadium(Long stadiumId) {
        return ResponseEntity.ok(restaurantQueryService.findByStadium(stadiumId));
    }
}
