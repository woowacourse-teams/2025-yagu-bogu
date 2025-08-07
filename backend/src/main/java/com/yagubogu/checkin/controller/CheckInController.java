package com.yagubogu.checkin.controller;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.checkin.domain.CheckInResultFilter;
import com.yagubogu.checkin.dto.CheckInCountsResponse;
import com.yagubogu.checkin.dto.CheckInHistoryResponse;
import com.yagubogu.checkin.dto.CheckInStatusResponse;
import com.yagubogu.checkin.dto.CreateCheckInRequest;
import com.yagubogu.checkin.dto.FanRateResponse;
import com.yagubogu.checkin.dto.VictoryFairyRankingResponses;
import com.yagubogu.checkin.service.CheckInService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/check-ins")
@RestController
public class CheckInController {

    private final CheckInService checkInService;

    @RequireRole
    @PostMapping
    public ResponseEntity<Void> createCheckIn(
            final MemberClaims memberClaims,
            @RequestBody final CreateCheckInRequest request
    ) {
        checkInService.createCheckIn(memberClaims.id(), request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/counts")
    public ResponseEntity<CheckInCountsResponse> findCheckInCounts(
            final MemberClaims memberClaims,
            @RequestParam final long year
    ) {
        CheckInCountsResponse response = checkInService.findCheckInCounts(memberClaims.id(), year);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/members")
    public ResponseEntity<CheckInHistoryResponse> findCheckInHistory(
            final MemberClaims memberClaims,
            @RequestParam final int year,
            @RequestParam(name = "result", defaultValue = "ALL") final CheckInResultFilter filter
    ) {
        CheckInHistoryResponse response = checkInService.findCheckInHistory(memberClaims.id(), year, filter);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stadiums/fan-rates")
    public ResponseEntity<FanRateResponse> findFanRatesByStadiums(
            final MemberClaims memberClaims,
            @RequestParam final LocalDate date
    ) {
        FanRateResponse response = checkInService.findFanRatesByGames(memberClaims.id(), date);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/victory-fairy/rankings")
    public ResponseEntity<VictoryFairyRankingResponses> findVictoryFairyRankings(
            final MemberClaims memberClaims
    ) {
        VictoryFairyRankingResponses response = checkInService.findVictoryFairyRankings(memberClaims.id());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<CheckInStatusResponse> findCheckInStatus(
            final MemberClaims memberClaims,
            @RequestParam final LocalDate date
    ) {
        CheckInStatusResponse response = checkInService.findCheckInStatus(memberClaims.id(), date);

        return ResponseEntity.ok(response);
    }
}
