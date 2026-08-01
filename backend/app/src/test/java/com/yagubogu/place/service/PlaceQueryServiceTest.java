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
import com.yagubogu.place.dto.v1.detail.AttractionDetail;
import com.yagubogu.place.dto.v1.detail.FoodDetail;
import com.yagubogu.place.dto.v1.detail.LodgingDetail;
import com.yagubogu.place.dto.v1.detail.PerformanceDetail;
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
                {"common":{"overview":"소개","homepage":"http://example.com"},"intro":{"opentimefood":"11:00~22:00","restdatefood":"연중무휴"}}
                """);
        given(placeRepository.findById(10L)).willReturn(Optional.of(place));

        PlaceDetailResponse response = placeQueryService.findDetailById(10L);

        assertThat(response.title()).isEqualTo("맛집A");
        assertThat(response.overview()).isEqualTo("소개");
        assertThat(response.homepage()).isEqualTo("http://example.com");
        assertThat(response.detail()).isInstanceOf(FoodDetail.class);
        FoodDetail detail = (FoodDetail) response.detail();
        assertThat(detail.opentimefood()).isEqualTo("11:00~22:00");
        assertThat(detail.restdatefood()).isEqualTo("연중무휴");
    }

    @DisplayName("ATTRACTION 카테고리는 detail이 AttractionDetail로 매핑된다")
    @Test
    void findDetailById_ATTRACTION_카테고리는_AttractionDetail로_매핑된다() {
        Place place = Place.of(PlaceCategory.ATTRACTION, "1603175", 1L, "석촌호수공원", "서울 송파구",
                127.07, 37.51, null, null, null);
        place.updateDetailInfo("""
                {"common":{"overview":"잠실 인근 도심 속 호수공원"},
                 "intro":{"infocenter":"송파구청 공원녹지과 02-2147-3392","opendate":"1986년 4월 30일",
                           "restdate":"연중무휴","usetime":"상시 개방","parking":"가능 (108대)",
                           "chkbabycarriage":"있음","chkcreditcard":"있음"}}
                """);
        given(placeRepository.findById(20L)).willReturn(Optional.of(place));

        PlaceDetailResponse response = placeQueryService.findDetailById(20L);

        assertThat(response.detail()).isInstanceOf(AttractionDetail.class);
        AttractionDetail detail = (AttractionDetail) response.detail();
        assertThat(detail.infocenter()).isEqualTo("송파구청 공원녹지과 02-2147-3392");
        assertThat(detail.restdate()).isEqualTo("연중무휴");
        assertThat(detail.parking()).isEqualTo("가능 (108대)");
        assertThat(detail.chkpet()).isNull();
    }

    @DisplayName("PERFORMANCE 카테고리는 detail이 PerformanceDetail로 매핑된다")
    @Test
    void findDetailById_PERFORMANCE_카테고리는_PerformanceDetail로_매핑된다() {
        Place place = Place.of(PlaceCategory.PERFORMANCE, "3439947", 1L, "강남 미디어 아트페스티벌", null,
                127.07, 37.51, null, null, null);
        place.updateDetailInfo("""
                {"common":{},
                 "intro":{"sponsor1":"강남아이즈 (Gangnam Eyes)","sponsor1tel":"02-6000-0114",
                           "eventstartdate":"20251219","eventenddate":"20260103",
                           "playtime":"11:00~22:00","eventplace":"잠실역 5, 6번 출구 잠실광장",
                           "usetimefestival":"무료"}}
                """);
        given(placeRepository.findById(21L)).willReturn(Optional.of(place));

        PlaceDetailResponse response = placeQueryService.findDetailById(21L);

        assertThat(response.detail()).isInstanceOf(PerformanceDetail.class);
        PerformanceDetail detail = (PerformanceDetail) response.detail();
        assertThat(detail.eventstartdate()).isEqualTo("20251219");
        assertThat(detail.eventenddate()).isEqualTo("20260103");
        assertThat(detail.sponsor1()).isEqualTo("강남아이즈 (Gangnam Eyes)");
        assertThat(detail.sponsor2()).isNull();
    }

    @DisplayName("LODGING 카테고리는 detail이 LodgingDetail로 매핑된다")
    @Test
    void findDetailById_LODGING_카테고리는_LodgingDetail로_매핑된다() {
        Place place = Place.of(PlaceCategory.LODGING, "3464974", 1L, "롯데호텔 월드", null,
                127.07, 37.51, null, null, null);
        place.updateDetailInfo("""
                {"common":{},
                 "intro":{"roomcount":"309실","checkintime":"15:00","checkouttime":"12:00",
                           "infocenterlodging":"02-2175-9000",
                           "parkinglodging":"지하주차장(자주식+기계식). 장소 인주차구역 2자리"}}
                """);
        given(placeRepository.findById(22L)).willReturn(Optional.of(place));

        PlaceDetailResponse response = placeQueryService.findDetailById(22L);

        assertThat(response.detail()).isInstanceOf(LodgingDetail.class);
        LodgingDetail detail = (LodgingDetail) response.detail();
        assertThat(detail.checkintime()).isEqualTo("15:00");
        assertThat(detail.checkouttime()).isEqualTo("12:00");
        assertThat(detail.infocenterlodging()).isEqualTo("02-2175-9000");
        assertThat(detail.reservationurl()).isNull();
    }

    @DisplayName("CAFE 카테고리도 RESTAURANT와 같은 FoodDetail을 공유한다 (둘 다 Tour API contentTypeId=39)")
    @Test
    void findDetailById_CAFE_카테고리도_RESTAURANT와_같은_FoodDetail을_공유한다() {
        Place place = Place.of(PlaceCategory.CAFE, "2869452", 1L, "카페엠", null,
                127.07, 37.51, null, null, null);
        place.updateDetailInfo("""
                {"common":{}, "intro":{"opentimefood":"09:00~22:00","firstmenu":"아메리카노"}}
                """);
        given(placeRepository.findById(23L)).willReturn(Optional.of(place));

        PlaceDetailResponse response = placeQueryService.findDetailById(23L);

        assertThat(response.detail()).isInstanceOf(FoodDetail.class);
        assertThat(((FoodDetail) response.detail()).firstmenu()).isEqualTo("아메리카노");
    }

    @DisplayName("intro에 우리 레코드에 없는 미지의 필드가 섞여 있어도 무시하고 파싱한다 "
            + "(Tour API가 필드를 추가해도 매핑이 깨지지 않아야 함)")
    @Test
    void findDetailById_알수없는_필드는_무시하고_파싱한다() {
        Place place = Place.of(PlaceCategory.RESTAURANT, "1001", 1L, "맛집A", null,
                127.07, 37.51, null, null, null);
        place.updateDetailInfo("""
                {"common":{}, "intro":{"opentimefood":"11:00~22:00","newFieldFromTourApi":"미래에 추가될 필드"}}
                """);
        given(placeRepository.findById(24L)).willReturn(Optional.of(place));

        PlaceDetailResponse response = placeQueryService.findDetailById(24L);

        assertThat(((FoodDetail) response.detail()).opentimefood()).isEqualTo("11:00~22:00");
    }

    @DisplayName("상세 정보가 아직 수집되지 않았으면 detail은 null이다")
    @Test
    void findDetailById_상세_정보_없으면_null() {
        Place place = Place.of(PlaceCategory.RESTAURANT, "1001", 1L, "맛집A", null,
                127.07, 37.51, null, null, null);
        given(placeRepository.findById(10L)).willReturn(Optional.of(place));

        PlaceDetailResponse response = placeQueryService.findDetailById(10L);

        assertThat(response.detail()).isNull();
        assertThat(response.overview()).isNull();
        assertThat(response.homepage()).isNull();
    }

    @DisplayName("존재하지 않는 장소 ID로 조회하면 NotFoundException을 던진다")
    @Test
    void findDetailById_존재하지_않으면_예외() {
        given(placeRepository.findById(any())).willReturn(Optional.empty());

        assertThatThrownBy(() -> placeQueryService.findDetailById(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
