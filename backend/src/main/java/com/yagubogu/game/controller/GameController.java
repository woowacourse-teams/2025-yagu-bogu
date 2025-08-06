package com.yagubogu.game.controller;

import com.yagubogu.game.dto.GameResponse;
import com.yagubogu.game.service.GameService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/games")
@RestController
public class GameController {

    private final GameService gameService;

    @GetMapping
    public ResponseEntity<GameResponse> findGamesByDate(
            @RequestParam final LocalDate date,
            @RequestParam final long memberId
    ) {
        GameResponse gameResponse = gameService.findGamesByDate(date, memberId);

        return ResponseEntity.ok(gameResponse);
    }
}
