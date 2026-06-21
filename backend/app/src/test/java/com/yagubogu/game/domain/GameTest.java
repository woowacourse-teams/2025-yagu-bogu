package com.yagubogu.game.domain;

import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.domain.StadiumLevel;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.domain.TeamStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GameTest {

    @DisplayName("진루정보/카운트를 갱신해도 현재 타자/투수는 영향받지 않는다")
    @Test
    void updateLiveBaseState_DoesNotAffectBatterAndPitcher() {
        // given
        Game game = makeGame();
        game.updateLiveBatterAndPitcher("away", "최지훈", "home", "김태경");

        // when
        game.updateLiveBaseState(true, false, true, 1, 2, 0);

        // then
        assertThat(game.getFirstBaseOccupied()).isTrue();
        assertThat(game.getSecondBaseOccupied()).isFalse();
        assertThat(game.getThirdBaseOccupied()).isTrue();
        assertThat(game.getBalls()).isEqualTo(1);
        assertThat(game.getStrikes()).isEqualTo(2);
        assertThat(game.getOuts()).isEqualTo(0);
        assertThat(game.getCurrentBatterTeam()).isEqualTo("away");
        assertThat(game.getCurrentBatterName()).isEqualTo("최지훈");
        assertThat(game.getCurrentPitcherTeam()).isEqualTo("home");
        assertThat(game.getCurrentPitcherName()).isEqualTo("김태경");
    }

    @DisplayName("현재 타자/투수를 갱신해도 진루정보/카운트는 영향받지 않는다")
    @Test
    void updateLiveBatterAndPitcher_DoesNotAffectBaseState() {
        // given
        Game game = makeGame();
        game.updateLiveBaseState(true, false, true, 1, 2, 0);

        // when
        game.updateLiveBatterAndPitcher("away", "최지훈", "home", "김태경");

        // then
        assertThat(game.getCurrentBatterTeam()).isEqualTo("away");
        assertThat(game.getCurrentBatterName()).isEqualTo("최지훈");
        assertThat(game.getCurrentPitcherTeam()).isEqualTo("home");
        assertThat(game.getCurrentPitcherName()).isEqualTo("김태경");
        assertThat(game.getFirstBaseOccupied()).isTrue();
        assertThat(game.getSecondBaseOccupied()).isFalse();
        assertThat(game.getThirdBaseOccupied()).isTrue();
        assertThat(game.getBalls()).isEqualTo(1);
        assertThat(game.getStrikes()).isEqualTo(2);
        assertThat(game.getOuts()).isEqualTo(0);
    }

    private Game makeGame() {
        Stadium stadium = new Stadium("잠실야구장", "잠실", "잠실", 37.5, 127.0, StadiumLevel.MAIN);
        Team homeTeam = new Team("LG 트윈스", "LG", "LG", TeamStatus.ACTIVE);
        Team awayTeam = new Team("두산 베어스", "두산", "OB", TeamStatus.ACTIVE);

        return new Game(
                stadium, homeTeam, awayTeam,
                LocalDate.of(2026, 6, 21), LocalTime.of(18, 30), "20260621OBLG0",
                null, null, null, null, null, null, GameState.LIVE
        );
    }
}
