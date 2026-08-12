package com.yagubogu.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.yagubogu.game.domain.BronzeGame;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.repository.BronzeGameRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BronzeGameServiceTest {

    private final BronzeGameRepository bronzeGameRepository = mock(BronzeGameRepository.class);
    private final BronzeGameService bronzeGameService = new BronzeGameService(bronzeGameRepository);

    @DisplayName("GameCenter의 공식 gameCode를 기존 예정 경기 Bronze에 연결한다")
    @Test
    void assignOfficialGameCodeToExistingBronzeGame() {
        LocalDate date = LocalDate.of(2026, 8, 13);
        LocalTime startTime = LocalTime.of(18, 30);
        String gameCode = "20260813HHOB0";
        BronzeGame bronzeGame = new BronzeGame(
                null, date, "잠실", "두산", "한화", startTime,
                LocalDateTime.of(2026, 8, 12, 0, 0), "{}", "hash"
        );
        bronzeGame.markEtlProcessed(LocalDateTime.of(2026, 8, 12, 0, 1));

        when(bronzeGameRepository.findByGameCode(gameCode)).thenReturn(Optional.empty());
        when(bronzeGameRepository.findByDateAndStadiumAndHomeTeamAndAwayTeamAndStartTime(
                date, "잠실", "두산", "한화", startTime
        )).thenReturn(Optional.of(bronzeGame));

        boolean updated = bronzeGameService.updateGameState(
                gameCode, date, "잠실", "두산", "한화", startTime, GameState.SCHEDULED
        );

        assertThat(updated).isTrue();
        assertThat(bronzeGame.getGameCode()).isEqualTo(gameCode);
        assertThat(bronzeGame.getEtlProcessedAt()).isNull();
    }

    @DisplayName("공식 gameCode가 새로 수집되면 시작 시각이 달라도 같은 Bronze 경기를 갱신한다")
    @Test
    void updateSameMatchupWhenStartTimeChanged() {
        LocalDate date = LocalDate.of(2026, 8, 11);
        String gameCode = "20260811KTNC0";
        BronzeGame bronzeGame = new BronzeGame(
                null, date, "창원", "NC", "KT", LocalTime.of(18, 30),
                LocalDateTime.of(2026, 8, 11, 0, 0), "old", "old-hash"
        );

        when(bronzeGameRepository.findByGameCode(gameCode)).thenReturn(Optional.empty());
        when(bronzeGameRepository.findByDateAndStadiumAndHomeTeamAndAwayTeamAndStartTime(
                date, "창원", "NC", "KT", LocalTime.of(19, 0)
        )).thenReturn(Optional.empty());
        when(bronzeGameRepository.findByDateAndStadiumAndHomeTeamAndAwayTeam(
                date, "창원", "NC", "KT"
        )).thenReturn(List.of(bronzeGame));

        boolean updated = bronzeGameService.upsertByNaturalKey(
                gameCode, date, "창원", "NC", "KT", LocalTime.of(19, 0), "new"
        );

        assertThat(updated).isTrue();
        assertThat(bronzeGame.getGameCode()).isEqualTo(gameCode);
        assertThat(bronzeGame.getStartTime()).isEqualTo(LocalTime.of(19, 0));
        assertThat(bronzeGame.getPayload()).isEqualTo("new");
    }

    @DisplayName("같은 gameCode의 시작 시각이 바뀌면 GameCenter 정보로 Bronze 메타데이터를 갱신한다")
    @Test
    void updateMetadataByGameCodeWhenStartTimeChanged() {
        LocalDate date = LocalDate.of(2026, 8, 13);
        String gameCode = "20260813HHOB0";
        BronzeGame bronzeGame = new BronzeGame(
                gameCode, date, "잠실", "두산", "한화", LocalTime.of(18, 30),
                LocalDateTime.of(2026, 8, 13, 0, 0), "{}", "hash"
        );
        bronzeGame.markEtlProcessed(LocalDateTime.of(2026, 8, 13, 0, 1));

        when(bronzeGameRepository.findByGameCode(gameCode)).thenReturn(Optional.of(bronzeGame));

        boolean updated = bronzeGameService.updateGameState(
                gameCode, date, "잠실", "두산", "한화", LocalTime.of(19, 0), GameState.SCHEDULED
        );

        assertThat(updated).isTrue();
        assertThat(bronzeGame.getStartTime()).isEqualTo(LocalTime.of(19, 0));
        assertThat(bronzeGame.getEtlProcessedAt()).isNull();
    }
}
