package com.yagubogu.widget.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;

/**
 * 위젯 푸시 페이로드에 담기는 실시간 점수 데이터.
 * iOS aps.content-state 및 Android data 메시지 모두에 사용됩니다.
 */
public record LiveScorePayload(
        Long gameId,
        TeamInfo homeTeam,
        TeamInfo awayTeam,
        String myTeamCode,
        int homeScore,
        int awayScore,
        int inning,
        String inningHalf,
        GameState gameState
) {

    public record TeamInfo(String code, String name) {
    }

    public static LiveScorePayload forStart(final Game game, final String myTeamCode) {
        return new LiveScorePayload(
                game.getId(),
                new TeamInfo(game.getHomeTeam().getTeamCode(), game.getHomeTeam().getName()),
                new TeamInfo(game.getAwayTeam().getTeamCode(), game.getAwayTeam().getName()),
                myTeamCode,
                0, 0, 0, "TOP",
                GameState.SCHEDULED
        );
    }

    public static LiveScorePayload fromGame(final Game game, final String myTeamCode) {
        return new LiveScorePayload(
                game.getId(),
                new TeamInfo(game.getHomeTeam().getTeamCode(), game.getHomeTeam().getName()),
                new TeamInfo(game.getAwayTeam().getTeamCode(), game.getAwayTeam().getName()),
                myTeamCode,
                game.getHomeScore() != null ? game.getHomeScore() : 0,
                game.getAwayScore() != null ? game.getAwayScore() : 0,
                0, "TOP",
                game.getGameState()
        );
    }
}
