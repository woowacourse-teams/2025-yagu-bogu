package com.yagubogu.stadium.controller.v1;

import com.yagubogu.stadium.dto.v1.StadiumsResponse;
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
