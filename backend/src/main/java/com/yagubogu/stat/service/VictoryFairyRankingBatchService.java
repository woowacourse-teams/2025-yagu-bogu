package com.yagubogu.stat.service;

import com.yagubogu.checkin.dto.VictoryFairyCountResult;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.checkin.repository.CustomCheckInRepository;
import com.yagubogu.stat.domain.VictoryFairyRanking;
import com.yagubogu.stat.dto.InsertDto;
import com.yagubogu.stat.dto.UpdateDto;
import com.yagubogu.stat.dto.VictoryFairyChunkResult;
import com.yagubogu.stat.repository.VictoryFairyRankingRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class VictoryFairyRankingBatchService {

    private final CheckInRepository checkInRepository;
    private final CustomCheckInRepository customCheckInRepository;
    private final VictoryFairyRankingRepository victoryFairyRankingRepository;

    private static final int CHUNK_SIZE = 2000;
    private static final int BATCH_SIZE = 200;

    public void updateRankings(final LocalDate date) {
        int currentYear = date.getYear();
        int page = 0;
        int totalProcessed = 0;
        int totalUpdated = 0;
        int totalInserted = 0;

        try {
            Slice<Long> slice;
            do {
                Pageable pageable = PageRequest.of(page, CHUNK_SIZE);
                slice = checkInRepository.findDistinctMemberIdsByDate(date, pageable);

                if (slice.hasContent()) {
                    List<Long> memberIds = slice.getContent();

                    VictoryFairyChunkResult result = processChunk(memberIds, currentYear);

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
            throw e;
        }
    }

    @Transactional
    public VictoryFairyChunkResult processChunk(List<Long> memberIds, int year) {
        Map<Long, VictoryFairyRanking> existingMap =
                victoryFairyRankingRepository.findByMemberIdsAndYear(memberIds, year)
                        .stream()
                        .collect(Collectors.toMap(
                                victoryFairyRanking -> victoryFairyRanking.getMember().getId(),
                                ranking -> ranking
                        ));

        List<VictoryFairyCountResult> checkInAndWinCounts = customCheckInRepository.findCheckInAndWinCountBatch(
                memberIds, year);

        List<UpdateDto> toUpdate = new ArrayList<>();
        List<InsertDto> toInsert = new ArrayList<>();

        double m = checkInRepository.calculateTotalAverageWinRate(year);
        double c = checkInRepository.calculateAverageCheckInCount(year);

        for (VictoryFairyCountResult checkInAndWinCount : checkInAndWinCounts) {
            int checkInCount = checkInAndWinCount.checkInCount();
            int winCount = checkInAndWinCount.winCount();

            double score = 100.0 * (winCount + c * m) / (checkInCount + c);
            double roundedScore = Math.round(score * 100) / 100.0;

            Long memberId = checkInAndWinCount.memberId();
            VictoryFairyRanking existingVictoryRanking = existingMap.get(memberId);

            if (existingVictoryRanking != null) {
                if (!isSameData(existingVictoryRanking, winCount, checkInCount, roundedScore)) {
                    toUpdate.add(new UpdateDto(
                            existingVictoryRanking.getId(),
                            score,
                            winCount,
                            checkInCount
                    ));
                    log.warn("Data consistency warning. {}, {}, {} vs {}, {}, {}",
                            existingVictoryRanking.getWinCount(), existingVictoryRanking.getCheckInCount(),
                            existingVictoryRanking.getScore(), winCount, checkInAndWinCount, score);
                }
            } else {
                toInsert.add(new InsertDto(
                        memberId,
                        year,
                        score,
                        winCount,
                        checkInCount
                ));
                log.warn("Data consistency warning. MemberId : {}", memberId);
            }
        }

        int updatedCount = 0;
        int insertedCount = 0;

        if (!toUpdate.isEmpty()) {
            victoryFairyRankingRepository.batchUpdate(toUpdate, BATCH_SIZE);
            updatedCount = toUpdate.size();
        }

        if (!toInsert.isEmpty()) {
            victoryFairyRankingRepository.batchInsert(toInsert, BATCH_SIZE);
            insertedCount = toInsert.size();
        }

        return new VictoryFairyChunkResult(updatedCount, insertedCount);
    }

    private boolean isSameData(final VictoryFairyRanking existingVictoryRanking, final int checkInCount,
                               final int winCount, final double roundedExpectedScore) {
        return existingVictoryRanking.getCheckInCount() == checkInCount
                && existingVictoryRanking.getWinCount() == winCount
                && Math.abs(existingVictoryRanking.getScore() - roundedExpectedScore) < 0.0001;
    }
}
