package com.yagubogu.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.yagubogu.place.client.TourApiClient;
import com.yagubogu.place.client.TourApiException;
import com.yagubogu.place.domain.Place;
import com.yagubogu.place.domain.PlaceCategory;
import com.yagubogu.place.dto.PlaceParam;
import com.yagubogu.place.repository.PlaceRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.domain.StadiumLevel;
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
class PlaceStadiumSyncServiceTest {

    @Mock
    private TourApiClient tourApiClient;

    @Mock
    private PlaceRepository placeRepository;

    @InjectMocks
    private PlaceStadiumSyncService placeStadiumSyncService;

    private Stadium jamsil;

    @BeforeEach
    void setUp() {
        jamsil = stadium(1L, "잠실", 37.5122, 127.0715);
    }

    @DisplayName("API에서 받은 신규 장소를 DB에 저장한다")
    @Test
    void syncForStadium_신규_장소_저장() {
        List<PlaceParam> fetched = List.of(
                new PlaceParam("1001", 1L, "맛집A", "서울 송파구", 127.07, 37.51, 300, "02-111", null)
        );
        given(tourApiClient.fetchPlacesNear(eq(PlaceCategory.RESTAURANT), anyLong(), anyDouble(), anyDouble()))
                .willReturn(fetched);
        given(placeRepository.findAllByStadiumIdAndCategory(1L, PlaceCategory.RESTAURANT)).willReturn(List.of());

        placeStadiumSyncService.syncForStadium(jamsil, PlaceCategory.RESTAURANT);

        ArgumentCaptor<List<Place>> captor = ArgumentCaptor.forClass(List.class);
        verify(placeRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getContentId()).isEqualTo("1001");
        assertThat(captor.getValue().get(0).getTitle()).isEqualTo("맛집A");
        assertThat(captor.getValue().get(0).getCategory()).isEqualTo(PlaceCategory.RESTAURANT);
        verify(placeRepository, never()).deleteAll(any());
    }

    @DisplayName("이미 존재하는 장소의 정보를 업데이트한다")
    @Test
    void syncForStadium_기존_장소_업데이트() {
        Place existing = Place.of(PlaceCategory.RESTAURANT, "1001", 1L, "구 이름", "구 주소",
                127.0, 37.5, 500, null, null);

        List<PlaceParam> fetched = List.of(
                new PlaceParam("1001", 1L, "새 이름", "새 주소", 127.07, 37.51, 300, "02-111", "https://img.com")
        );
        given(tourApiClient.fetchPlacesNear(eq(PlaceCategory.RESTAURANT), anyLong(), anyDouble(), anyDouble()))
                .willReturn(fetched);
        given(placeRepository.findAllByStadiumIdAndCategory(1L, PlaceCategory.RESTAURANT))
                .willReturn(List.of(existing));

        placeStadiumSyncService.syncForStadium(jamsil, PlaceCategory.RESTAURANT);

        ArgumentCaptor<List<Place>> captor = ArgumentCaptor.forClass(List.class);
        verify(placeRepository).saveAll(captor.capture());
        Place updated = captor.getValue().get(0);
        assertThat(updated.getTitle()).isEqualTo("새 이름");
        assertThat(updated.getAddress()).isEqualTo("새 주소");
        assertThat(updated.getDistance()).isEqualTo(300);
        assertThat(updated.getImageUrl()).isEqualTo("https://img.com");
    }

    @DisplayName("API 응답에 없는 기존 장소는 삭제한다")
    @Test
    void syncForStadium_사라진_장소_삭제() {
        Place stale = Place.of(PlaceCategory.RESTAURANT, "GONE", 1L, "없어진 맛집", null,
                127.0, 37.5, null, null, null);

        List<PlaceParam> fetched = List.of(
                new PlaceParam("NEW", 1L, "새 맛집", null, 127.07, 37.51, 100, null, null)
        );
        given(tourApiClient.fetchPlacesNear(eq(PlaceCategory.RESTAURANT), anyLong(), anyDouble(), anyDouble()))
                .willReturn(fetched);
        given(placeRepository.findAllByStadiumIdAndCategory(1L, PlaceCategory.RESTAURANT))
                .willReturn(List.of(stale));

        placeStadiumSyncService.syncForStadium(jamsil, PlaceCategory.RESTAURANT);

        ArgumentCaptor<List<Place>> deleteCaptor = ArgumentCaptor.forClass(List.class);
        verify(placeRepository).deleteAll(deleteCaptor.capture());
        assertThat(deleteCaptor.getValue()).hasSize(1);
        assertThat(deleteCaptor.getValue().get(0).getContentId()).isEqualTo("GONE");
    }

    @DisplayName("API 호출이 실패하면 재시도하고, 성공 시 동기화를 완료한다")
    @Test
    void syncForStadium_재시도_후_성공() {
        List<PlaceParam> fetched = List.of(
                new PlaceParam("1001", 1L, "맛집A", null, 127.07, 37.51, 100, null, null)
        );
        given(tourApiClient.fetchPlacesNear(eq(PlaceCategory.RESTAURANT), anyLong(), anyDouble(), anyDouble()))
                .willThrow(new TourApiException("timeout", new RuntimeException()))
                .willReturn(fetched);
        given(placeRepository.findAllByStadiumIdAndCategory(1L, PlaceCategory.RESTAURANT)).willReturn(List.of());

        placeStadiumSyncService.syncForStadium(jamsil, PlaceCategory.RESTAURANT);

        verify(tourApiClient, times(2))
                .fetchPlacesNear(eq(PlaceCategory.RESTAURANT), anyLong(), anyDouble(), anyDouble());
        verify(placeRepository).saveAll(any());
    }

    @DisplayName("재시도를 모두 소진하고 API 호출이 최종 실패하면 예외를 던지고, 기존 장소는 건드리지 않는다")
    @Test
    void syncForStadium_재시도_모두_실패하면_예외_발생하고_기존_데이터_보존() {
        given(tourApiClient.fetchPlacesNear(eq(PlaceCategory.RESTAURANT), anyLong(), anyDouble(), anyDouble()))
                .willThrow(new TourApiException("Tour API non-OK response: code=99", null));

        assertThatThrownBy(() -> placeStadiumSyncService.syncForStadium(jamsil, PlaceCategory.RESTAURANT))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("All retries exhausted");

        // API 오류를 "결과 없음"으로 오인해 기존 장소를 지우면 안 된다 (재현 대상 버그)
        verify(placeRepository, never()).findAllByStadiumIdAndCategory(any(), any());
        verify(placeRepository, never()).saveAll(any());
        verify(placeRepository, never()).deleteAll(any());
    }

    @DisplayName("API 응답이 정상적으로 비어있으면(반경 내 결과 없음) 기존 데이터를 모두 삭제한다")
    @Test
    void syncForStadium_API_빈_응답_기존_데이터_삭제() {
        Place existing = Place.of(PlaceCategory.RESTAURANT, "OLD", 1L, "기존 맛집", null,
                127.0, 37.5, null, null, null);

        given(tourApiClient.fetchPlacesNear(eq(PlaceCategory.RESTAURANT), anyLong(), anyDouble(), anyDouble()))
                .willReturn(List.of());
        given(placeRepository.findAllByStadiumIdAndCategory(1L, PlaceCategory.RESTAURANT))
                .willReturn(List.of(existing));

        placeStadiumSyncService.syncForStadium(jamsil, PlaceCategory.RESTAURANT);

        ArgumentCaptor<List<Place>> saveCaptor = ArgumentCaptor.forClass(List.class);
        verify(placeRepository).saveAll(saveCaptor.capture());
        assertThat(saveCaptor.getValue()).isEmpty();

        ArgumentCaptor<List<Place>> deleteCaptor = ArgumentCaptor.forClass(List.class);
        verify(placeRepository).deleteAll(deleteCaptor.capture());
        assertThat(deleteCaptor.getValue()).isNotEmpty();
    }

    @DisplayName("신규 장소는 상세 정보를 조회해 저장한다")
    @Test
    void syncForStadium_신규_장소_상세_정보_수집() {
        List<PlaceParam> fetched = List.of(
                new PlaceParam("1001", 1L, "맛집A", null, 127.07, 37.51, 100, null, null)
        );
        given(tourApiClient.fetchPlacesNear(eq(PlaceCategory.RESTAURANT), anyLong(), anyDouble(), anyDouble()))
                .willReturn(fetched);
        given(placeRepository.findAllByStadiumIdAndCategory(1L, PlaceCategory.RESTAURANT)).willReturn(List.of());
        given(tourApiClient.fetchDetail(eq("1001"), eq(39))).willReturn("{\"common\":{}}");

        placeStadiumSyncService.syncForStadium(jamsil, PlaceCategory.RESTAURANT);

        verify(tourApiClient).fetchDetail("1001", 39);
        ArgumentCaptor<List<Place>> captor = ArgumentCaptor.forClass(List.class);
        verify(placeRepository).saveAll(captor.capture());
        assertThat(captor.getValue().get(0).getDetailInfo()).isEqualTo("{\"common\":{}}");
    }

    @DisplayName("이미 상세 정보가 있는 장소는 다시 조회하지 않는다")
    @Test
    void syncForStadium_기존_상세_정보_있으면_재조회_안함() {
        Place existing = Place.of(PlaceCategory.RESTAURANT, "1001", 1L, "구 이름", "구 주소",
                127.0, 37.5, 500, null, null);
        existing.updateDetailInfo("{\"common\":{\"overview\":\"기존 정보\"}}");

        List<PlaceParam> fetched = List.of(
                new PlaceParam("1001", 1L, "새 이름", "새 주소", 127.07, 37.51, 300, "02-111", null)
        );
        given(tourApiClient.fetchPlacesNear(eq(PlaceCategory.RESTAURANT), anyLong(), anyDouble(), anyDouble()))
                .willReturn(fetched);
        given(placeRepository.findAllByStadiumIdAndCategory(1L, PlaceCategory.RESTAURANT))
                .willReturn(List.of(existing));

        placeStadiumSyncService.syncForStadium(jamsil, PlaceCategory.RESTAURANT);

        verify(tourApiClient, never()).fetchDetail(anyString(), anyInt());
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
