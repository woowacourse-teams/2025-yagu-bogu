package com.yagubogu.restaurant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.yagubogu.restaurant.client.TourApiClient;
import com.yagubogu.restaurant.client.TourApiException;
import com.yagubogu.restaurant.domain.Restaurant;
import com.yagubogu.restaurant.dto.RestaurantParam;
import com.yagubogu.restaurant.repository.RestaurantRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.domain.StadiumLevel;
import com.yagubogu.stadium.repository.StadiumRepository;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestaurantSyncServiceTest {

    @Mock
    private TourApiClient tourApiClient;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private StadiumRepository stadiumRepository;

    @InjectMocks
    private RestaurantSyncService restaurantSyncService;

    private Stadium jamsil;
    private Stadium suwon;

    @BeforeEach
    void setUp() {
        jamsil = stadium(1L, "잠실", 37.5122, 127.0715);
        suwon = stadium(2L, "수원", 37.2998, 127.0097);
    }

    @DisplayName("API에서 받은 신규 맛집을 DB에 저장한다")
    @Test
    void syncForStadium_신규_맛집_저장() {
        List<RestaurantParam> fetched = List.of(
                new RestaurantParam("1001", 1L, "맛집A", "서울 송파구", 127.07, 37.51, 300, "02-111", null)
        );
        given(tourApiClient.fetchRestaurantsNear(anyLong(), anyDouble(), anyDouble())).willReturn(fetched);
        given(restaurantRepository.findAllByStadiumId(1L)).willReturn(List.of());

        restaurantSyncService.syncForStadium(jamsil);

        ArgumentCaptor<List<Restaurant>> captor = ArgumentCaptor.forClass(List.class);
        verify(restaurantRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getContentId()).isEqualTo("1001");
        assertThat(captor.getValue().get(0).getTitle()).isEqualTo("맛집A");
        verify(restaurantRepository, never()).deleteAll(any());
    }

    @DisplayName("이미 존재하는 맛집의 정보를 업데이트한다")
    @Test
    void syncForStadium_기존_맛집_업데이트() {
        Restaurant existing = Restaurant.of("1001", 1L, "구 이름", "구 주소", 127.0, 37.5, 500, null, null);

        List<RestaurantParam> fetched = List.of(
                new RestaurantParam("1001", 1L, "새 이름", "새 주소", 127.07, 37.51, 300, "02-111", "https://img.com")
        );
        given(tourApiClient.fetchRestaurantsNear(anyLong(), anyDouble(), anyDouble())).willReturn(fetched);
        given(restaurantRepository.findAllByStadiumId(1L)).willReturn(List.of(existing));

        restaurantSyncService.syncForStadium(jamsil);

        ArgumentCaptor<List<Restaurant>> captor = ArgumentCaptor.forClass(List.class);
        verify(restaurantRepository).saveAll(captor.capture());
        Restaurant updated = captor.getValue().get(0);
        assertThat(updated.getTitle()).isEqualTo("새 이름");
        assertThat(updated.getAddress()).isEqualTo("새 주소");
        assertThat(updated.getDistance()).isEqualTo(300);
        assertThat(updated.getImageUrl()).isEqualTo("https://img.com");
    }

    @DisplayName("API 응답에 없는 기존 맛집은 삭제한다")
    @Test
    void syncForStadium_사라진_맛집_삭제() {
        Restaurant stale = Restaurant.of("GONE", 1L, "없어진 맛집", null, 127.0, 37.5, null, null, null);

        List<RestaurantParam> fetched = List.of(
                new RestaurantParam("NEW", 1L, "새 맛집", null, 127.07, 37.51, 100, null, null)
        );
        given(tourApiClient.fetchRestaurantsNear(anyLong(), anyDouble(), anyDouble())).willReturn(fetched);
        given(restaurantRepository.findAllByStadiumId(1L)).willReturn(List.of(stale));

        restaurantSyncService.syncForStadium(jamsil);

        ArgumentCaptor<List<Restaurant>> deleteCaptor = ArgumentCaptor.forClass(List.class);
        verify(restaurantRepository).deleteAll(deleteCaptor.capture());
        assertThat(deleteCaptor.getValue()).hasSize(1);
        assertThat(deleteCaptor.getValue().get(0).getContentId()).isEqualTo("GONE");
    }

    @DisplayName("API 호출이 실패하면 재시도하고, 성공 시 동기화를 완료한다")
    @Test
    void syncForStadium_재시도_후_성공() {
        List<RestaurantParam> fetched = List.of(
                new RestaurantParam("1001", 1L, "맛집A", null, 127.07, 37.51, 100, null, null)
        );
        given(tourApiClient.fetchRestaurantsNear(anyLong(), anyDouble(), anyDouble()))
                .willThrow(new TourApiException("timeout", new RuntimeException()))
                .willReturn(fetched);
        given(restaurantRepository.findAllByStadiumId(1L)).willReturn(List.of());

        restaurantSyncService.syncForStadium(jamsil);

        verify(tourApiClient, times(2)).fetchRestaurantsNear(anyLong(), anyDouble(), anyDouble());
        verify(restaurantRepository).saveAll(any());
    }

    @DisplayName("syncAll은 한 구장이 실패해도 나머지 구장의 동기화를 계속한다")
    @Test
    void syncAll_한_구장_실패해도_나머지_진행() {
        given(stadiumRepository.findAll()).willReturn(List.of(jamsil, suwon));
        given(tourApiClient.fetchRestaurantsNear(anyLong(), anyDouble(), anyDouble()))
                .willThrow(new TourApiException("fail", new RuntimeException()));

        restaurantSyncService.syncAll();

        // 2개 구장 × 최대 3회 재시도 = 6번 호출
        verify(tourApiClient, times(6)).fetchRestaurantsNear(anyLong(), anyDouble(), anyDouble());
        verify(restaurantRepository, never()).saveAll(any());
    }

    @DisplayName("API 응답이 비어있으면 기존 데이터를 모두 삭제한다")
    @Test
    void syncForStadium_API_빈_응답_기존_데이터_삭제() {
        Restaurant existing = Restaurant.of("OLD", 1L, "기존 맛집", null, 127.0, 37.5, null, null, null);

        given(tourApiClient.fetchRestaurantsNear(anyLong(), anyDouble(), anyDouble())).willReturn(List.of());
        given(restaurantRepository.findAllByStadiumId(1L)).willReturn(List.of(existing));

        restaurantSyncService.syncForStadium(jamsil);

        ArgumentCaptor<List<Restaurant>> saveCaptor = ArgumentCaptor.forClass(List.class);
        verify(restaurantRepository).saveAll(saveCaptor.capture());
        assertThat(saveCaptor.getValue()).isEmpty();

        ArgumentCaptor<List<Restaurant>> deleteCaptor = ArgumentCaptor.forClass(List.class);
        verify(restaurantRepository).deleteAll(deleteCaptor.capture());
        assertThat(deleteCaptor.getValue()).isNotEmpty();
    }

    // Stadium은 @GeneratedValue이므로 리플렉션으로 ID 주입
    private static Stadium stadium(long id, String shortName, double lat, double lon) {
        Stadium s = new Stadium("full-" + shortName, shortName, "loc", lat, lon, StadiumLevel.MAIN);
        try {
            Field f = Stadium.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(s, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return s;
    }
}
