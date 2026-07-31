package com.yagubogu.place.service;

import com.yagubogu.place.client.TourApiClient;
import com.yagubogu.place.client.TourApiException;
import com.yagubogu.place.domain.Place;
import com.yagubogu.place.domain.PlaceCategory;
import com.yagubogu.place.dto.PlaceParam;
import com.yagubogu.place.repository.PlaceRepository;
import com.yagubogu.stadium.domain.Stadium;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 구장 하나 x 카테고리 하나를 동기화하는 트랜잭션 단위.
 * {@link PlaceSyncService}가 이 빈을 통해서(프록시를 거쳐서) 호출해야
 * {@link Transactional}이 정상적으로 적용된다 — 같은 클래스 안의 self-invocation으로
 * 호출하면 Spring AOP 프록시를 우회해 트랜잭션이 걸리지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceStadiumSyncService {

    private static final int MAX_RETRIES = 3;

    private final TourApiClient tourApiClient;
    private final PlaceRepository placeRepository;

    @Transactional
    public void syncForStadium(Stadium stadium, PlaceCategory category) {
        Long stadiumId = stadium.getId();

        // mapX = longitude, mapY = latitude (KTO API 명세 기준)
        // API 호출이 최종 실패하면 예외가 던져지며, 이 시점 이후의 삭제 로직은 실행되지 않는다.
        // (API 오류를 "결과 없음"으로 오인해 기존 장소를 전부 지우는 것을 방지)
        List<PlaceParam> fetched = fetchWithRetry(stadium, category);

        Map<String, Place> existing = placeRepository.findAllByStadiumIdAndCategory(stadiumId, category)
                .stream()
                .collect(Collectors.toMap(Place::getContentId, p -> p));

        Set<String> fetchedContentIds = fetched.stream()
                .map(PlaceParam::contentId)
                .collect(Collectors.toSet());

        List<Place> toSave = fetched.stream()
                .map(param -> {
                    Place place = existing.get(param.contentId());
                    if (place != null) {
                        place.update(param.title(), param.address(), param.mapX(), param.mapY(),
                                param.distance(), param.tel(), param.imageUrl());
                        return place;
                    }
                    return Place.of(category, param.contentId(), stadiumId, param.title(), param.address(),
                            param.mapX(), param.mapY(), param.distance(), param.tel(), param.imageUrl());
                })
                .toList();

        placeRepository.saveAll(toSave);
        syncDetailInfo(toSave);

        List<Place> toDelete = existing.values().stream()
                .filter(p -> !fetchedContentIds.contains(p.getContentId()))
                .toList();

        if (!toDelete.isEmpty()) {
            placeRepository.deleteAll(toDelete);
        }

        log.info("[PlaceSync] stadiumId={} ({}) category={}: upserted={}, deleted={}",
                stadiumId, stadium.getShortName(), category, toSave.size(), toDelete.size());
    }

    /**
     * 상세 정보는 자주 바뀌지 않고 조회 빈도도 낮으므로, 최초 1회만 수집해 API 호출량을 아낀다.
     * 이미 detailInfo가 있는 장소는 다시 요청하지 않는다.
     */
    private void syncDetailInfo(List<Place> places) {
        for (Place place : places) {
            if (place.hasDetailInfo()) {
                continue;
            }
            String detail = tourApiClient.fetchDetail(place.getContentId(), place.getContentTypeId());
            if (detail != null) {
                place.updateDetailInfo(detail);
            }
        }
    }

    private List<PlaceParam> fetchWithRetry(Stadium stadium, PlaceCategory category) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return tourApiClient.fetchPlacesNear(
                        category, stadium.getId(), stadium.getLongitude(), stadium.getLatitude());
            } catch (TourApiException e) {
                lastException = e;
                log.warn("[PlaceSync] Attempt {}/{} failed for stadiumId={} category={}: {}",
                        attempt, MAX_RETRIES, stadium.getId(), category, e.getMessage());
                if (attempt < MAX_RETRIES) {
                    sleepExponential(attempt);
                }
            }
        }

        throw new RuntimeException(
                "All retries exhausted for stadiumId=" + stadium.getId() + " category=" + category, lastException);
    }

    private void sleepExponential(int attempt) {
        try {
            long delayMs = 1000L << (attempt - 1); // 1s, 2s, 4s
            Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
