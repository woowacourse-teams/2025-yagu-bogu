package com.yagubogu.game.service.crawler.KboScoardboardCrawler;

import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.ScoreBoard;
import com.yagubogu.game.dto.KboScoreboardTeam;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class KboScoreboardMapper {

    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;

    Team resolveTeamFromShortName(String shortName) {
        return teamRepository.findByShortName(shortName)
                .orElseThrow(() -> new IllegalArgumentException("팀 매핑 실패: " + shortName));
    }

    Stadium resolveStadium(String stadiumText) {
        if (stadiumText != null && !stadiumText.isBlank()) {
            return stadiumRepository.findByLocation(stadiumText.trim())
                    .orElseThrow(() -> new IllegalArgumentException("구장(단축명) 매핑 실패: " + stadiumText));
        }
        throw new IllegalArgumentException("구장 정보가 비어있습니다.");
    }

    ScoreBoard toScoreBoard(KboScoreboardTeam t) {
        // null 방어 + 이닝 스코어 정리
        Integer runs = t.runs() == null ? 0 : t.runs();
        Integer hits = t.hits() == null ? 0 : t.hits();
        Integer errs = t.errors() == null ? 0 : t.errors();
        Integer bb = t.basesOnBalls() == null ? 0 : t.basesOnBalls();
        List<String> innings = t.inningScores() == null ? List.of() : t.inningScores();
        return new ScoreBoard(runs, hits, errs, bb, innings);
    }

    /**
     * "경기전", "경기중", "경기종료" 등 상태 문자열을 GameState로 매핑
     */
    GameState toState(String statusText, Integer homeRuns, Integer awayRuns) {
        if (statusText == null) {
            return GameState.SCHEDULED;
        }
        String s = statusText.trim();
        if (s.contains("전")) {
            return GameState.SCHEDULED;
        }
        if (s.contains("중")) {
            return GameState.LIVE;
        }
        if (s.contains("종료") || s.contains("끝")) {
            return GameState.COMPLETED;
        }

        // 점수 유무로 보정
        if (homeRuns != null || awayRuns != null) {
            return GameState.LIVE;
        }
        return GameState.SCHEDULED;
    }
}

