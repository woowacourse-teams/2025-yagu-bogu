package com.yagubogu.stat.controller;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.stat.dto.LuckyStadiumResponse;
import com.yagubogu.stat.dto.StatCountsResponse;
import com.yagubogu.stat.dto.WinRateResponse;
import com.yagubogu.stat.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequireRole
@RequestMapping("/api/stats")
@RestController
public class StatController {

    private final StatService statService;

    @GetMapping("/counts")
    public ResponseEntity<StatCountsResponse> findStatCounts(
            final MemberClaims memberClaims,
            @RequestParam final int year
    ) {
        StatCountsResponse response = statService.findStatCounts(memberClaims.id(), year);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/win-rate")
    public ResponseEntity<WinRateResponse> findWinRate(
            final MemberClaims memberClaims,
            @RequestParam final int year
    ) {
        WinRateResponse response = statService.findWinRate(memberClaims.id(), year);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/lucky-stadiums")
    public ResponseEntity<LuckyStadiumResponse> findLuckyStadiums(
            final MemberClaims memberClaims,
            @RequestParam final int year
    ) {
        LuckyStadiumResponse response = statService.findLuckyStadium(memberClaims.id(), year);

        return ResponseEntity.ok(response);
    }
}
