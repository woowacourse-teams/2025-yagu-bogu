package com.yagubogu.stat.service;

import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.stat.dto.VictoryFairyChunkResult;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class StatSyncService {

    private static final int CHUNK_SIZE = 100000;

    private final VictoryFairyRankingSyncService victoryFairyRankingSyncService;
    private final CheckInRepository checkInRepository;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void updateRankings(final LocalDate date) {
        int currentYear = date.getYear();
        int page = 0;
        int totalProcessed = 0;
        int totalUpdated = 0;
        int totalInserted = 0;

        double averageWinRate = checkInRepository.calculateTotalAverageWinRate(date.getYear());
        double averageCheckInCount = checkInRepository.calculateAverageCheckInCount(date.getYear());

        try {
            Slice<Long> slice;
            do {
                Pageable pageable = PageRequest.of(page, CHUNK_SIZE);
                slice = checkInRepository.findDistinctMemberIdsByDate(date, pageable);

                if (slice.hasContent()) {
                    List<Long> memberIds = slice.getContent();

                    VictoryFairyChunkResult result = victoryFairyRankingSyncService.processChunk(memberIds,
                            currentYear, averageWinRate, averageCheckInCount);

                    totalProcessed += memberIds.size();
                    totalUpdated += result.updatedCount();
                    totalInserted += result.insertedCount();

                    log.info("Progress: page {}, {} members processed (updated: {}, inserted: {})",
                            page, totalProcessed, totalUpdated, totalInserted);
                }
                page++;

            } while (slice.hasNext());

            log.info("=== Batch Completed === total: {}, updated: {}, inserted: {}, skipped: {}",
                    totalProcessed, totalUpdated, totalInserted,
                    totalProcessed - totalUpdated - totalInserted);
        } catch (RuntimeException e) {
            log.error("Batch failed", e);
        }
    }
}
