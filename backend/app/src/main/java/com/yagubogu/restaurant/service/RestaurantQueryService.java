package com.yagubogu.restaurant.service;

import com.yagubogu.restaurant.dto.v1.RestaurantResponse;
import com.yagubogu.restaurant.dto.v1.RestaurantsResponse;
import com.yagubogu.restaurant.repository.RestaurantRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestaurantQueryService {

    private final RestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public RestaurantsResponse findByStadium(Long stadiumId) {
        List<RestaurantResponse> restaurants = restaurantRepository.findAllByStadiumId(stadiumId)
                .stream()
                .map(RestaurantResponse::from)
                .toList();

        return new RestaurantsResponse(stadiumId, restaurants);
    }
}
