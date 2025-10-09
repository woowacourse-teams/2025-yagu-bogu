package com.yagubogu.stat.service;

import com.yagubogu.checkin.dto.VictoryFairyCountResult;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.stat.domain.VictoryFairyRanking;
import com.yagubogu.stat.dto.InsertDto;
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

    @Transactional
    public VictoryFairyChunkResult processChunk(List<Long> memberIds, int year) {
        Map<Long, VictoryFairyRanking> existingMap =
                victoryFairyRankingRepository.findByMemberIdsAndYear(memberIds, year)
                        .stream()
                        .collect(Collectors.toMap(
                                victoryFairyRanking -> victoryFairyRanking.getMember().getId(),
                                ranking -> ranking
                        ));

        List<VictoryFairyCountResult> checkInAndWinCounts = checkInRepository.findCheckInAndWinCountBatch(
                memberIds, year);

        List<UpdateDto> toUpdate = new ArrayList<>();
        List<InsertDto> toInsert = new ArrayList<>();

        double m = checkInRepository.calculateTotalAverageWinRate(year);
        double c = checkInRepository.calculateAverageCheckInCount(year);

        for (VictoryFairyCountResult checkInAndWinCount : checkInAndWinCounts) {
            int checkInCount = checkInAndWinCount.checkInCount();
            int winCount = checkInAndWinCount.winCount();

            double roundedScore = getScore((winCount + c * m) / (checkInCount + c));

            Long memberId = checkInAndWinCount.memberId();
            VictoryFairyRanking existingVictoryRanking = existingMap.get(memberId);

            if (existingVictoryRanking != null) {
                if (!isSameData(existingVictoryRanking, winCount, checkInCount, roundedScore)) {
                    toUpdate.add(new UpdateDto(
                            existingVictoryRanking.getId(),
                            roundedScore,
                            winCount,
                            checkInCount
                    ));
                    log.warn("Data consistency warning. {}, {}, {} vs {}, {}, {}",
                            existingVictoryRanking.getWinCount(), existingVictoryRanking.getCheckInCount(),
                            existingVictoryRanking.getScore(), winCount, checkInAndWinCount, roundedScore);
                }
            } else {
                toInsert.add(new InsertDto(
                        memberId,
                        year,
                        roundedScore,
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

    private double getScore(double value) {
        return BigDecimal.valueOf(value)
                .multiply(BigDecimal.valueOf(100)) // 100.0 *
                .setScale(2, RoundingMode.HALF_UP) // ROUND(..., 2)
                .doubleValue();
    }

    private boolean isSameData(final VictoryFairyRanking existingVictoryRanking, final int checkInCount,
                               final int winCount, final double roundedExpectedScore) {
        return existingVictoryRanking.getCheckInCount() == checkInCount
                && existingVictoryRanking.getWinCount() == winCount
                && Math.abs(existingVictoryRanking.getScore() - roundedExpectedScore) < 0.0001;
    }
}
