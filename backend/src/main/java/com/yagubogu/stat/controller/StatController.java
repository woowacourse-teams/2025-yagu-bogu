package com.yagubogu.stat.controller;

import com.yagubogu.stat.dto.StatCountsResponse;
import com.yagubogu.stat.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/stats")
@RestController
public class StatController {

    private final StatService statService;

    // TODO : ArgumentResolver 구현
    @GetMapping("/counts")
    public ResponseEntity<StatCountsResponse> findStatCounts(
            @RequestParam final long memberId,
            @RequestParam final int year
    ) {
        StatCountsResponse response = statService.findStatCounts(memberId, year);
        return ResponseEntity.ok(response);
    }
}
