package com.yagubogu.stadium.controller;

import com.yagubogu.stadium.dto.StadiumsResponse;
import com.yagubogu.stadium.service.StadiumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/stadiums")
@RestController
public class StadiumController {

    private final StadiumService stadiumService;

    @GetMapping
    public ResponseEntity<StadiumsResponse> findStadiums() {
        StadiumsResponse actual = stadiumService.findAll();
        return ResponseEntity.ok(actual);
    }
}
