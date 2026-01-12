package com.yagubogu.stat.controller.v1;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.checkin.dto.v1.TeamFilter;
import com.yagubogu.checkin.dto.v1.VictoryFairyRankingResponse;
import com.yagubogu.stat.dto.v1.AverageStatisticResponse;
import com.yagubogu.stat.dto.v1.LuckyStadiumResponse;
import com.yagubogu.stat.dto.v1.OpponentWinRateResponse;
import com.yagubogu.stat.dto.v1.RecentGamesWinRateResponse;
import com.yagubogu.stat.dto.v1.StatCountsResponse;
import com.yagubogu.stat.dto.v1.WinRateResponse;
import com.yagubogu.stat.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequireRole
@RestController
public class StatController implements StatControllerInterface {

    private final StatService statService;

    public ResponseEntity<StatCountsResponse> findStatCounts(
            final MemberClaims memberClaims,
            @RequestParam final int year
    ) {
        StatCountsResponse response = statService.findStatCounts(memberClaims.id(), year);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<WinRateResponse> findWinRate(
            final MemberClaims memberClaims,
            @RequestParam final int year
    ) {
        WinRateResponse response = statService.findWinRate(memberClaims.id(), year);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<RecentGamesWinRateResponse> findRecentTenGamesWinRate(
            final MemberClaims memberClaims,
            @RequestParam final int year
    ) {
        RecentGamesWinRateResponse response = statService.findRecentTenGamesWinRate(memberClaims.id(), year);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<LuckyStadiumResponse> findLuckyStadiums(
            final MemberClaims memberClaims,
            @RequestParam final int year
    ) {
        LuckyStadiumResponse response = statService.findLuckyStadium(memberClaims.id(), year);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<AverageStatisticResponse> findAverageStatistic(
            final MemberClaims memberClaims
    ) {
        AverageStatisticResponse response = statService.findAverageStatistic(memberClaims.id());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<OpponentWinRateResponse> findOpponentWinRate(
            final MemberClaims memberClaims,
            @RequestParam final int year
    ) {
        OpponentWinRateResponse response = statService.findOpponentWinRate(memberClaims.id(), year);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<VictoryFairyRankingResponse> findVictoryFairyRankings(
            final MemberClaims memberClaims,
            @RequestParam(name = "team", defaultValue = "ALL") final TeamFilter teamFilter,
            @RequestParam(required = false) final Integer year
    ) {
        VictoryFairyRankingResponse response = statService.findVictoryFairyRankings(memberClaims.id(), teamFilter,
                year);

        return ResponseEntity.ok(response);
    }
}
