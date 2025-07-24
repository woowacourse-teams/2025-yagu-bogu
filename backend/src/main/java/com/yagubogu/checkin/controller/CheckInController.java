package com.yagubogu.checkin.controller;

import com.yagubogu.checkin.dto.CheckInCountsResponse;
import com.yagubogu.checkin.dto.CreateCheckInRequest;
import com.yagubogu.checkin.service.CheckInService;
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

    @PostMapping
    public ResponseEntity<Void> createCheckIn(
            @RequestBody CreateCheckInRequest request
    ) {
        checkInService.createCheckIn(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/counts")
    public ResponseEntity<CheckInCountsResponse> findCheckInCounts(
            @RequestParam long memberId,
            @RequestParam long year
    ) {
        CheckInCountsResponse response = checkInService.findCheckInCounts(memberId, year);
        
        return ResponseEntity.ok(response);
    }
}
