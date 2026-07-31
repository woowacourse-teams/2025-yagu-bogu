package com.yagubogu.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.place.domain.Place;
import com.yagubogu.place.domain.PlaceCategory;
import com.yagubogu.place.dto.v1.PlaceDetailResponse;
import com.yagubogu.place.dto.v1.PlacesResponse;
import com.yagubogu.place.repository.PlaceRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaceQueryServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    private PlaceQueryService placeQueryService;

    @BeforeEach
    void setUp() {
        placeQueryService = new PlaceQueryService(placeRepository, new ObjectMapper());
    }

    @DisplayName("구장 ID와 카테고리로 장소 목록을 반환한다")
    @Test
    void findByStadiumAndCategory_장소_목록_반환() {
        Long stadiumId = 1L;
        List<Place> places = List.of(
                Place.of(PlaceCategory.RESTAURANT, "1001", stadiumId, "맛집A", "서울 송파구",
                        127.07, 37.51, 300, "02-111", null),
                Place.of(PlaceCategory.RESTAURANT, "1002", stadiumId, "맛집B", "서울 송파구",
                        127.08, 37.52, 500, null, "https://img.com")
        );
        given(placeRepository.findAllByStadiumIdAndCategory(stadiumId, PlaceCategory.RESTAURANT)).willReturn(places);

        PlacesResponse response = placeQueryService.findByStadiumAndCategory(stadiumId, PlaceCategory.RESTAURANT);

        assertThat(response.stadiumId()).isEqualTo(stadiumId);
        assertThat(response.category()).isEqualTo(PlaceCategory.RESTAURANT);
        assertThat(response.places()).hasSize(2);
        assertThat(response.places()).extracting(p -> p.title())
                .containsExactly("맛집A", "맛집B");
        assertThat(response.places().get(0).distance()).isEqualTo(300);
        assertThat(response.places().get(1).imageUrl()).isEqualTo("https://img.com");
    }

    @DisplayName("해당 구장/카테고리에 장소가 없으면 빈 리스트를 반환한다")
    @Test
    void findByStadiumAndCategory_장소_없으면_빈_리스트() {
        Long stadiumId = 99L;
        given(placeRepository.findAllByStadiumIdAndCategory(stadiumId, PlaceCategory.CAFE)).willReturn(List.of());

        PlacesResponse response = placeQueryService.findByStadiumAndCategory(stadiumId, PlaceCategory.CAFE);

        assertThat(response.stadiumId()).isEqualTo(stadiumId);
        assertThat(response.places()).isEmpty();
    }

    @DisplayName("응답에 mapX, mapY 좌표가 포함된다")
    @Test
    void findByStadiumAndCategory_좌표_포함_반환() {
        Long stadiumId = 2L;
        Place place = Place.of(PlaceCategory.ATTRACTION, "2001", stadiumId, "좌표관광지", null,
                127.0715, 37.5122, null, null, null);
        given(placeRepository.findAllByStadiumIdAndCategory(stadiumId, PlaceCategory.ATTRACTION))
                .willReturn(List.of(place));

        PlacesResponse response = placeQueryService.findByStadiumAndCategory(stadiumId, PlaceCategory.ATTRACTION);

        assertThat(response.places().get(0).mapX()).isEqualTo(127.0715);
        assertThat(response.places().get(0).mapY()).isEqualTo(37.5122);
    }

    @DisplayName("장소 ID로 상세 정보를 조회하면 detailInfo가 JSON으로 파싱되어 반환된다")
    @Test
    void findDetailById_상세_정보_반환() {
        Place place = Place.of(PlaceCategory.RESTAURANT, "1001", 1L, "맛집A", "서울 송파구",
                127.07, 37.51, 300, "02-111", null);
        place.updateDetailInfo("""
                {"common":{"overview":"소개"},"intro":{"opentimefood":"11:00~22:00"}}
                """);
        given(placeRepository.findById(10L)).willReturn(Optional.of(place));

        PlaceDetailResponse response = placeQueryService.findDetailById(10L);

        assertThat(response.title()).isEqualTo("맛집A");
        assertThat(response.detail().path("common").path("overview").asText()).isEqualTo("소개");
        assertThat(response.detail().path("intro").path("opentimefood").asText()).isEqualTo("11:00~22:00");
    }

    @DisplayName("상세 정보가 아직 수집되지 않았으면 detail은 null이다")
    @Test
    void findDetailById_상세_정보_없으면_null() {
        Place place = Place.of(PlaceCategory.RESTAURANT, "1001", 1L, "맛집A", null,
                127.07, 37.51, null, null, null);
        given(placeRepository.findById(10L)).willReturn(Optional.of(place));

        PlaceDetailResponse response = placeQueryService.findDetailById(10L);

        assertThat(response.detail()).isNull();
    }

    @DisplayName("존재하지 않는 장소 ID로 조회하면 NotFoundException을 던진다")
    @Test
    void findDetailById_존재하지_않으면_예외() {
        given(placeRepository.findById(any())).willReturn(Optional.empty());

        assertThatThrownBy(() -> placeQueryService.findDetailById(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
