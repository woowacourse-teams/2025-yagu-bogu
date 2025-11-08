package com.yagubogu.game.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.dto.GameResultParam;
import com.yagubogu.game.dto.GameWithCheckInParam;
import com.yagubogu.game.dto.v1.GameResponse;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.global.exception.UnprocessableEntityException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class GameService {

    private final GameRepository gameRepository;
    private final MemberRepository memberRepository;

    public GameResponse findGamesByDate(
            final LocalDate date,
            final long memberId
    ) {
        Member member = getMember(memberId);
        validateIsNotFuture(date);

        List<GameWithCheckInParam> gameWithCheckInParams = gameRepository.findGamesWithCheckInsByDate(date, member);

        return new GameResponse(gameWithCheckInParams);
    }

    public GameResultParam findScoreBoard(final long gameId) {
        Game game = getGame(gameId);
        validateScoreBoard(game);

        return GameResultParam.from(game);
    }

    public boolean isLiveToday() {
        LocalDate today = LocalDate.now();

        return gameRepository.existsByDateAndGameState(today, GameState.LIVE);
    }

    private static void validateScoreBoard(final Game game) {
        if (game.getHomeScoreBoard() == null || game.getAwayScoreBoard() == null || game.getHomePitcher() == null
                || game.getAwayPitcher() == null) {
            throw new NotFoundException("ScoreBoard not found");
        }
    }

    private Member getMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }

    private Game getGame(final Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));
    }

    private void validateIsNotFuture(final LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            throw new UnprocessableEntityException("Cannot retrieve games for future dates");
        }
    }
}
