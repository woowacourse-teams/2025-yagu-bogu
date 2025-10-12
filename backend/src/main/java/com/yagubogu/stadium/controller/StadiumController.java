package com.yagubogu.stadium.controller;

import com.yagubogu.stadium.dto.StadiumsResponse;
import com.yagubogu.stadium.dto.StadiumsWithGamesResponse;
import com.yagubogu.stadium.service.StadiumService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class StadiumController implements StadiumControllerInterface {

    private final StadiumService stadiumService;

    public ResponseEntity<StadiumsResponse> findStadiums() {
        StadiumsResponse response = stadiumService.findAll();

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<StadiumsWithGamesResponse> findStadiumsWithGame(
            @RequestParam final LocalDate date
    ) {
        StadiumsWithGamesResponse response = stadiumService.findWithGameByDate(date);

        return ResponseEntity.ok(response);
    }
}
