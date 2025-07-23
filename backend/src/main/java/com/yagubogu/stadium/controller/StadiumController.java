package com.yagubogu.stadium.controller;

import com.yagubogu.stadium.dto.StadiumsResponse;
import com.yagubogu.stadium.dto.TeamOccupancyRatesResponse;
import com.yagubogu.stadium.service.StadiumService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/stadiums")
@RestController
public class StadiumController {

    private final StadiumService stadiumService;

    @GetMapping
    public ResponseEntity<StadiumsResponse> findStadiums() {
        StadiumsResponse response = stadiumService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{stadiumId}/occupancy-rate")
    public ResponseEntity<TeamOccupancyRatesResponse> findOccupancyRate(
            @PathVariable final long stadiumId,
            @RequestParam final LocalDate date
    ) {
        TeamOccupancyRatesResponse response = stadiumService.findOccupancyRate(stadiumId, date);
        return ResponseEntity.ok(response);
    }
}
