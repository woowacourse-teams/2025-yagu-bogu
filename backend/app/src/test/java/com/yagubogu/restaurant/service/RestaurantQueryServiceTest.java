package com.yagubogu.restaurant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.yagubogu.restaurant.domain.Restaurant;
import com.yagubogu.restaurant.dto.v1.RestaurantsResponse;
import com.yagubogu.restaurant.repository.RestaurantRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestaurantQueryServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantQueryService restaurantQueryService;

    @DisplayName("구장 ID로 맛집 목록을 반환한다")
    @Test
    void findByStadium_맛집_목록_반환() {
        Long stadiumId = 1L;
        List<Restaurant> restaurants = List.of(
                Restaurant.of("1001", stadiumId, "맛집A", "서울 송파구", 127.07, 37.51, 300, "02-111", null),
                Restaurant.of("1002", stadiumId, "맛집B", "서울 송파구", 127.08, 37.52, 500, null, "https://img.com")
        );
        given(restaurantRepository.findAllByStadiumId(stadiumId)).willReturn(restaurants);

        RestaurantsResponse response = restaurantQueryService.findByStadium(stadiumId);

        assertThat(response.stadiumId()).isEqualTo(stadiumId);
        assertThat(response.restaurants()).hasSize(2);
        assertThat(response.restaurants()).extracting(r -> r.title())
                .containsExactly("맛집A", "맛집B");
        assertThat(response.restaurants().get(0).distance()).isEqualTo(300);
        assertThat(response.restaurants().get(1).imageUrl()).isEqualTo("https://img.com");
    }

    @DisplayName("해당 구장에 맛집이 없으면 빈 리스트를 반환한다")
    @Test
    void findByStadium_맛집_없으면_빈_리스트() {
        Long stadiumId = 99L;
        given(restaurantRepository.findAllByStadiumId(stadiumId)).willReturn(List.of());

        RestaurantsResponse response = restaurantQueryService.findByStadium(stadiumId);

        assertThat(response.stadiumId()).isEqualTo(stadiumId);
        assertThat(response.restaurants()).isEmpty();
    }

    @DisplayName("응답에 mapX, mapY 좌표가 포함된다")
    @Test
    void findByStadium_좌표_포함_반환() {
        Long stadiumId = 2L;
        Restaurant restaurant = Restaurant.of("2001", stadiumId, "좌표맛집", null, 127.0715, 37.5122, null, null, null);
        given(restaurantRepository.findAllByStadiumId(stadiumId)).willReturn(List.of(restaurant));

        RestaurantsResponse response = restaurantQueryService.findByStadium(stadiumId);

        assertThat(response.restaurants().get(0).mapX()).isEqualTo(127.0715);
        assertThat(response.restaurants().get(0).mapY()).isEqualTo(37.5122);
    }
}
