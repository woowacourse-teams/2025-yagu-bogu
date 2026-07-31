package com.yagubogu.place.service;

import com.yagubogu.place.client.TourApiClient;
import com.yagubogu.place.client.TourApiException;
import com.yagubogu.place.domain.Place;
import com.yagubogu.place.domain.PlaceCategory;
import com.yagubogu.place.dto.PlaceParam;
import com.yagubogu.place.repository.PlaceRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceSyncService {

    private static final int MAX_RETRIES = 3;

    private final TourApiClient tourApiClient;
    private final PlaceRepository placeRepository;
    private final StadiumRepository stadiumRepository;

    public void syncAll() {
        List<Stadium> stadiums = stadiumRepository.findAll();
        log.info("[PlaceSync] Starting sync for {} stadiums x {} categories",
                stadiums.size(), PlaceCategory.values().length);

        int success = 0;
        int failed = 0;

        for (Stadium stadium : stadiums) {
            for (PlaceCategory category : PlaceCategory.values()) {
                try {
                    syncForStadium(stadium, category);
                    success++;
                } catch (Exception e) {
                    log.error("[PlaceSync] Failed to sync stadiumId={} ({}) category={}: {}",
                            stadium.getId(), stadium.getShortName(), category, e.getMessage());
                    failed++;
                }
            }
        }

        log.info("[PlaceSync] Completed: success={}, failed={}", success, failed);
    }

    @Transactional
    public void syncForStadium(Stadium stadium, PlaceCategory category) {
        Long stadiumId = stadium.getId();

        // mapX = longitude, mapY = latitude (KTO API 명세 기준)
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
