package com.yagubogu.stadium.controller;

import com.yagubogu.stadium.dto.StadiumsResponse;
import com.yagubogu.stadium.service.StadiumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class StadiumController implements StadiumControllerInterface {

    private final StadiumService stadiumService;

    public ResponseEntity<StadiumsResponse> findStadiums() {
        StadiumsResponse response = stadiumService.findAll();
        return ResponseEntity.ok(response);
    }
}
