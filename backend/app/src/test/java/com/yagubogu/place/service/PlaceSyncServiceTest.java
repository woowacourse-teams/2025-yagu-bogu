package com.yagubogu.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.yagubogu.place.domain.PlaceCategory;
import com.yagubogu.place.dto.PlaceSyncResult;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.domain.StadiumLevel;
import com.yagubogu.stadium.repository.StadiumRepository;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    @DisplayName("syncAll은 성공/실패한 조합 수를 집계해 반환한다")
    @Test
    void syncAll_성공_실패_집계() {
        Stadium jamsil = stadium(1L, "잠실", 37.5122, 127.0715);
        given(stadiumRepository.findAll()).willReturn(List.of(jamsil));
        // 특정 조합만 실패시킨다. 인자를 좁혀 stubbing하면 나머지 호출이 strict stubs에 걸려
        // PotentialStubbingProblem이 발생하고, 그것까지 실패로 집계되어 검증이 흐려진다.
        willAnswer(invocation -> {
            if (invocation.getArgument(1) == PlaceCategory.RESTAURANT) {
                throw new RuntimeException("sync failed");
            }
            return null;
        }).given(placeStadiumSyncService).syncForStadium(any(Stadium.class), any(PlaceCategory.class));

        Optional<PlaceSyncResult> actual = placeSyncService.syncAll();

        // 1개 구장 x 5개 카테고리 중 RESTAURANT 하나만 실패
        assertThat(actual).contains(new PlaceSyncResult(4, 1));
    }

    @DisplayName("이미 동기화가 진행 중이면 중복 실행하지 않고 빈 결과를 반환한다")
    @Test
    void syncAll_이미_진행_중이면_중복_실행하지_않음() {
        Stadium jamsil = stadium(1L, "잠실", 37.5122, 127.0715);
        given(stadiumRepository.findAll()).willReturn(List.of(jamsil));

        // 동기화가 진행 중인 동안 다시 트리거된 상황(스케줄러와 관리자 수동 트리거가 겹친 경우)
        List<Optional<PlaceSyncResult>> reentrantResults = new ArrayList<>();
        willAnswer(invocation -> {
            reentrantResults.add(placeSyncService.syncAll());
            return null;
        }).given(placeStadiumSyncService).syncForStadium(any(Stadium.class), any(PlaceCategory.class));

        Optional<PlaceSyncResult> actual = placeSyncService.syncAll();

        assertThat(actual).contains(new PlaceSyncResult(5, 0));
        assertThat(reentrantResults).hasSize(5).allMatch(Optional::isEmpty);
        // 중복 트리거가 무시되므로 위임은 5번(1 구장 x 5 카테고리)만 일어난다
        verify(placeStadiumSyncService, times(5))
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
