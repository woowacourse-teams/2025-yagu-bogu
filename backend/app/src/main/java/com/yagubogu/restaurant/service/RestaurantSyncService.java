package com.yagubogu.restaurant.service;

import com.yagubogu.restaurant.client.TourApiClient;
import com.yagubogu.restaurant.client.TourApiException;
import com.yagubogu.restaurant.domain.Restaurant;
import com.yagubogu.restaurant.dto.RestaurantParam;
import com.yagubogu.restaurant.repository.RestaurantRepository;
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
public class RestaurantSyncService {

    private static final int MAX_RETRIES = 3;

    private final TourApiClient tourApiClient;
    private final RestaurantRepository restaurantRepository;
    private final StadiumRepository stadiumRepository;

    public void syncAll() {
        List<Stadium> stadiums = stadiumRepository.findAll();
        log.info("[RestaurantSync] Starting sync for {} stadiums", stadiums.size());

        int success = 0;
        int failed = 0;

        for (Stadium stadium : stadiums) {
            try {
                syncForStadium(stadium);
                success++;
            } catch (Exception e) {
                log.error("[RestaurantSync] Failed to sync stadiumId={} ({}): {}",
                        stadium.getId(), stadium.getShortName(), e.getMessage());
                failed++;
            }
        }

        log.info("[RestaurantSync] Completed: success={}, failed={}", success, failed);
    }

    @Transactional
    public void syncForStadium(Stadium stadium) {
        Long stadiumId = stadium.getId();

        // mapX = longitude, mapY = latitude (KTO API 명세 기준)
        List<RestaurantParam> fetched = fetchWithRetry(stadium);

        Map<String, Restaurant> existing = restaurantRepository.findAllByStadiumId(stadiumId)
                .stream()
                .collect(Collectors.toMap(Restaurant::getContentId, r -> r));

        Set<String> fetchedContentIds = fetched.stream()
                .map(RestaurantParam::contentId)
                .collect(Collectors.toSet());

        List<Restaurant> toSave = fetched.stream()
                .map(param -> {
                    Restaurant r = existing.get(param.contentId());
                    if (r != null) {
                        r.update(param.title(), param.address(), param.mapX(), param.mapY(),
                                param.distance(), param.tel(), param.imageUrl());
                        return r;
                    }
                    return Restaurant.of(param.contentId(), stadiumId, param.title(), param.address(),
                            param.mapX(), param.mapY(), param.distance(), param.tel(), param.imageUrl());
                })
                .toList();

        restaurantRepository.saveAll(toSave);

        List<Restaurant> toDelete = existing.values().stream()
                .filter(r -> !fetchedContentIds.contains(r.getContentId()))
                .toList();

        if (!toDelete.isEmpty()) {
            restaurantRepository.deleteAll(toDelete);
        }

        log.info("[RestaurantSync] stadiumId={} ({}): upserted={}, deleted={}",
                stadiumId, stadium.getShortName(), toSave.size(), toDelete.size());
    }

    private List<RestaurantParam> fetchWithRetry(Stadium stadium) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return tourApiClient.fetchRestaurantsNear(
                        stadium.getId(), stadium.getLongitude(), stadium.getLatitude());
            } catch (TourApiException e) {
                lastException = e;
                log.warn("[RestaurantSync] Attempt {}/{} failed for stadiumId={}: {}",
                        attempt, MAX_RETRIES, stadium.getId(), e.getMessage());
                if (attempt < MAX_RETRIES) {
                    sleepExponential(attempt);
                }
            }
        }

        throw new RuntimeException("All retries exhausted for stadiumId=" + stadium.getId(), lastException);
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
