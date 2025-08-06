package com.yagubogu.game.controller;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.MemberClaims;
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

    @RequireRole
    @GetMapping
    public ResponseEntity<GameResponse> findGamesByDate(
            final MemberClaims memberClaims,
            @RequestParam final LocalDate date
    ) {
        GameResponse response = gameService.findGamesByDate(date, memberClaims.id());

        return ResponseEntity.ok(response);
    }
}
