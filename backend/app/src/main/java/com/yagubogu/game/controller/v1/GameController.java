package com.yagubogu.game.controller.v1;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.game.dto.v1.GameResponse;
import com.yagubogu.game.service.GameService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class GameController implements GameControllerInterface {

    private final GameService gameService;

    @RequireRole
    public ResponseEntity<GameResponse> findGamesByDate(
            final MemberClaims memberClaims,
            @RequestParam final LocalDate date
    ) {
        GameResponse response = gameService.findGamesByDate(date, memberClaims.id());

        return ResponseEntity.ok(response);
    }
}
