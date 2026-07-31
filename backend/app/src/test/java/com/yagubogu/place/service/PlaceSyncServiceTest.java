package com.yagubogu.place.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.yagubogu.place.domain.PlaceCategory;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.domain.StadiumLevel;
import com.yagubogu.stadium.repository.StadiumRepository;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * syncAll()의 오케스트레이션만 검증한다. 구장 하나에 대한 실제 동기화 로직은
 * {@link PlaceStadiumSyncServiceTest}에서 다룬다 (트랜잭션 빈이 분리되어 있음).
 */
@ExtendWith(MockitoExtension.class)
class PlaceSyncServiceTest {

    @Mock
    private PlaceStadiumSyncService placeStadiumSyncService;

    @Mock
    private StadiumRepository stadiumRepository;

    @InjectMocks
    private PlaceSyncService placeSyncService;

    @DisplayName("syncAll은 모든 구장 x 카테고리 조합에 대해 동기화를 위임한다")
    @Test
    void syncAll_모든_구장_카테고리_조합에_대해_위임() {
        Stadium jamsil = stadium(1L, "잠실", 37.5122, 127.0715);
        Stadium suwon = stadium(2L, "수원", 37.2998, 127.0097);
        given(stadiumRepository.findAll()).willReturn(List.of(jamsil, suwon));

        placeSyncService.syncAll();

        // 2개 구장 x 5개 카테고리 = 10번 위임
        verify(placeStadiumSyncService, times(10))
                .syncForStadium(any(Stadium.class), any(PlaceCategory.class));
    }

    @DisplayName("한 구장/카테고리 조합에서 동기화가 실패해도 나머지 조합은 계속 진행한다")
    @Test
    void syncAll_한_조합_실패해도_나머지_계속_진행() {
        Stadium jamsil = stadium(1L, "잠실", 37.5122, 127.0715);
        Stadium suwon = stadium(2L, "수원", 37.2998, 127.0097);
        given(stadiumRepository.findAll()).willReturn(List.of(jamsil, suwon));
        willThrow(new RuntimeException("sync failed"))
                .given(placeStadiumSyncService).syncForStadium(any(Stadium.class), any(PlaceCategory.class));

        placeSyncService.syncAll();

        // 예외가 나도 순회 자체는 10번(2 구장 x 5 카테고리) 다 시도된다
        verify(placeStadiumSyncService, times(10))
                .syncForStadium(any(Stadium.class), any(PlaceCategory.class));
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
