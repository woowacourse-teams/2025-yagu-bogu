package com.yagubogu.stat.service;

import com.yagubogu.checkin.dto.VictoryFairyCountResult;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.stat.domain.VictoryFairyRanking;
import com.yagubogu.stat.dto.InsertDto;
import com.yagubogu.stat.dto.SyncBatchData;
import com.yagubogu.stat.dto.UpdateDto;
import com.yagubogu.stat.dto.VictoryFairyChunkResult;
import com.yagubogu.stat.repository.VictoryFairyRankingRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class VictoryFairyRankingSyncService {

    private final CheckInRepository checkInRepository;
    private final VictoryFairyRankingRepository victoryFairyRankingRepository;

    private static final int BATCH_SIZE = 200;
    private static final int SCORE_SCALE = 2;
    private static final double SCORE_PERCENTAGE_MULTIPLIER = 100.0;
    private static final double SCORE_COMPARISON_THRESHOLD = 0.0001;

    @Transactional
    public VictoryFairyChunkResult processChunk(List<Long> memberIds, int year) {
        Map<Long, VictoryFairyRanking> existingRankingMap = buildExistingRankingMap(memberIds, year);
        List<VictoryFairyCountResult> checkInAndWinCounts = checkInRepository.findCheckInAndWinCountBatch(
                memberIds, year);

        double averageWinRate = checkInRepository.calculateTotalAverageWinRate(year);
        double averageCheckInCount = checkInRepository.calculateAverageCheckInCount(year);

        SyncBatchData batchData = prepareBatchData(
                checkInAndWinCounts,
                existingRankingMap,
                averageWinRate,
                averageCheckInCount,
                year
        );

        return executeBatchOperations(batchData);
    }

    private Map<Long, VictoryFairyRanking> buildExistingRankingMap(final List<Long> memberIds, final int year) {
        return victoryFairyRankingRepository.findByMemberIdsAndYear(memberIds, year)
                .stream()
                .collect(Collectors.toMap(
                        ranking -> ranking.getMember().getId(),
                        ranking -> ranking
                ));
    }

    private SyncBatchData prepareBatchData(
            List<VictoryFairyCountResult> checkInAndWinCounts,
            Map<Long, VictoryFairyRanking> existingRankingMap,
            double averageWinRate,
            double averageCheckInCount,
            int year
    ) {
        List<UpdateDto> toUpdate = new ArrayList<>();
        List<InsertDto> toInsert = new ArrayList<>();

        for (VictoryFairyCountResult result : checkInAndWinCounts) {
            int checkInCount = result.checkInCount();
            int winCount = result.winCount();
            Long memberId = result.memberId();

            double calculatedScore = calculateVictoryFairyScore(
                    winCount,
                    checkInCount,
                    averageWinRate,
                    averageCheckInCount
            );

            VictoryFairyRanking existingRanking = existingRankingMap.get(memberId);

            if (existingRanking != null) {
                processExistingRanking(existingRanking, winCount, checkInCount, calculatedScore, toUpdate);
            } else {
                processNewRanking(memberId, year, winCount, checkInCount, calculatedScore, toInsert);
            }
        }

        return new SyncBatchData(toUpdate, toInsert);
    }

    private double calculateVictoryFairyScore(
            int winCount,
            int checkInCount,
            double averageWinRate,
            double averageCheckInCount
    ) {
        double rawScore = (winCount + averageCheckInCount * averageWinRate)
                / (checkInCount + averageCheckInCount);
        return getScore(rawScore);
    }

    private void processExistingRanking(
            VictoryFairyRanking existingRanking,
            int winCount,
            int checkInCount,
            double calculatedScore,
            List<UpdateDto> toUpdate
    ) {
        if (!isSameData(existingRanking, winCount, checkInCount, calculatedScore)) {
            toUpdate.add(new UpdateDto(
                    existingRanking.getId(),
                    calculatedScore,
                    winCount,
                    checkInCount
            ));
            logDataInconsistency(existingRanking, winCount, checkInCount, calculatedScore);
        }
    }

    private void processNewRanking(
            Long memberId,
            int year,
            int winCount,
            int checkInCount,
            double calculatedScore,
            List<InsertDto> toInsert
    ) {
        toInsert.add(new InsertDto(
                memberId,
                year,
                calculatedScore,
                winCount,
                checkInCount
        ));
        logMissingRanking(memberId);
    }

    private VictoryFairyChunkResult executeBatchOperations(SyncBatchData batchData) {
        int updatedCount = 0;
        int insertedCount = 0;

        if (!batchData.toUpdate().isEmpty()) {
            victoryFairyRankingRepository.batchUpdate(batchData.toUpdate(), BATCH_SIZE);
            updatedCount = batchData.toUpdate().size();
        }

        if (!batchData.toInsert().isEmpty()) {
            victoryFairyRankingRepository.batchInsert(batchData.toInsert(), BATCH_SIZE);
            insertedCount = batchData.toInsert().size();
        }

        return new VictoryFairyChunkResult(updatedCount, insertedCount);
    }

    private double getScore(double value) {
        return BigDecimal.valueOf(value)
                .multiply(BigDecimal.valueOf(SCORE_PERCENTAGE_MULTIPLIER))
                .setScale(SCORE_SCALE, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private boolean isSameData(final VictoryFairyRanking existingVictoryRanking, final int winCount,
                               final int checkInCount, final double roundedExpectedScore) {
        return existingVictoryRanking.getCheckInCount() == checkInCount
                && existingVictoryRanking.getWinCount() == winCount
                && Math.abs(existingVictoryRanking.getScore() - roundedExpectedScore) < SCORE_COMPARISON_THRESHOLD;
    }

    private void logDataInconsistency(
            VictoryFairyRanking existingRanking,
            int expectedWinCount,
            int expectedCheckInCount,
            double expectedScore
    ) {
        log.warn("[Data Inconsistency] memberId={}, existing=[winCount={}, checkInCount={}, score={}], " +
                        "expected=[winCount={}, checkInCount={}, score={}]",
                existingRanking.getMember().getId(),
                existingRanking.getWinCount(), existingRanking.getCheckInCount(), existingRanking.getScore(),
                expectedWinCount, expectedCheckInCount, expectedScore);
    }

    private void logMissingRanking(Long memberId) {
        log.warn("[Missing Ranking Data] memberId={} - No existing ranking found, will be inserted", memberId);
    }
}
