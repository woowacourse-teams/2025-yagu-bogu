package com.yagubogu.restaurant.service;

import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantSyncService {

    private final RestaurantSyncTransactionService restaurantSyncTransactionService;
    private final StadiumRepository stadiumRepository;

    public void syncAll() {
        List<Stadium> stadiums = stadiumRepository.findAll();
        log.info("[RestaurantSync] Starting sync for {} stadiums", stadiums.size());

        int success = 0;
        int failed = 0;

        for (Stadium stadium : stadiums) {
            try {
                restaurantSyncTransactionService.syncForStadium(stadium);
                success++;
            } catch (Exception e) {
                log.error("[RestaurantSync] Failed to sync stadiumId={} ({}): {}",
                        stadium.getId(), stadium.getShortName(), e.getMessage());
                failed++;
            }
        }

        log.info("[RestaurantSync] Completed: success={}, failed={}", success, failed);
    }
}
