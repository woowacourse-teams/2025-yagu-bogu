package com.yagubogu.checkin.controller;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.checkin.domain.CheckInOrderFilter;
import com.yagubogu.checkin.domain.CheckInResultFilter;
import com.yagubogu.checkin.dto.CheckInCountsResponse;
import com.yagubogu.checkin.dto.CheckInHistoryResponse;
import com.yagubogu.checkin.dto.CheckInStatusResponse;
import com.yagubogu.checkin.dto.CreateCheckInRequest;
import com.yagubogu.checkin.dto.FanRateResponse;
import com.yagubogu.checkin.dto.StadiumCheckInCountsResponse;
import com.yagubogu.checkin.service.CheckInService;
import com.yagubogu.stat.service.StatService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequireRole
@RestController
public class CheckInController implements CheckInControllerInterface {

    private final CheckInService checkInService;
    private final StatService statService;

    public ResponseEntity<Void> createCheckIn(
            final MemberClaims memberClaims,
            @RequestBody final CreateCheckInRequest request
    ) {
        checkInService.createCheckIn(memberClaims.id(), request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public ResponseEntity<CheckInCountsResponse> findCheckInCounts(
            final MemberClaims memberClaims,
            @RequestParam final int year
    ) {
        CheckInCountsResponse response = checkInService.findCheckInCounts(memberClaims.id(), year);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CheckInHistoryResponse> findCheckInHistory(
            final MemberClaims memberClaims,
            @RequestParam final int year,
            @RequestParam(name = "result", defaultValue = "ALL") final CheckInResultFilter resultFilter,
            @RequestParam(name = "order", defaultValue = "LATEST") final CheckInOrderFilter orderFilter
    ) {
        CheckInHistoryResponse response = checkInService.findCheckInHistory(
                memberClaims.id(),
                year,
                resultFilter,
                orderFilter
        );

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<FanRateResponse> findFanRatesByStadiums(
            final MemberClaims memberClaims,
            @RequestParam final LocalDate date
    ) {
        FanRateResponse response = checkInService.findFanRatesByGames(memberClaims.id(), date);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<CheckInStatusResponse> findCheckInStatus(
            final MemberClaims memberClaims,
            @RequestParam final LocalDate date
    ) {
        CheckInStatusResponse response = checkInService.findCheckInStatus(memberClaims.id(), date);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<StadiumCheckInCountsResponse> findStadiumCheckInCount(
            final MemberClaims memberClaims,
            @RequestParam final int year
    ) {
        StadiumCheckInCountsResponse response = checkInService.findStadiumCheckInCounts(memberClaims.id(), year);

        return ResponseEntity.ok(response);
    }
}
