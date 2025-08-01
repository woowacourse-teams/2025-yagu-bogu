package com.yagubogu.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.yagubogu.fixture.TestFixture;
import com.yagubogu.game.dto.KboClientResponse;
import com.yagubogu.game.dto.KboGameItemDto;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.game.service.client.KboClient;
import com.yagubogu.global.exception.ClientException;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
@DataJpaTest
class GameServiceTest {

    private GameService gameService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Mock
    private KboClient kboClient;

    @BeforeEach
    void setUp() {
        gameService = new GameService(kboClient, gameRepository, teamRepository, stadiumRepository);
    }

    @DisplayName("경기 목록을 성공적으로 가져와서 저장한다")
    @Test
    void fetchGameList() {
        // given
        LocalDate today = TestFixture.getToday();
        KboGameItemDto gameItem = new KboGameItemDto(
                "20250721OBLG0", "2025-07-21", 0L, "18:30",
                "잠실", "기아", "두산", "정상경기", "", "정규시즌", "5", "3"
        );
        KboClientResponse response = new KboClientResponse(List.of(gameItem), "100", "success");

        given(kboClient.fetchGame(today)).willReturn(response);

        // when
        gameService.fetchGameList(today);

        // then
        assertThat(gameRepository.findAll()
                .stream()
                .filter(game ->
                        game.getGameCode().equals(gameItem.gameCode()))
                .findFirst())
                .isPresent();
    }

    @DisplayName("예외 : 경기장을 찾을 수 없으면 예외가 발생한다")
    @Test
    void fetchGameList_stadiumNotFound() {
        // given
        LocalDate today = TestFixture.getToday();
        KboGameItemDto gameItem = new KboGameItemDto(
                "20250721SSHH0", "2025-07-21", 0L, "18:30",
                "존재하지않는경기장", "한화", "삼성", "정상경기", "", "정규시즌", "5", "3"
        );
        KboClientResponse response = new KboClientResponse(List.of(gameItem), "100", "success");

        given(kboClient.fetchGame(today)).willReturn(response);

        // when & then
        assertThatThrownBy(() -> gameService.fetchGameList(today))
                .isInstanceOf(ClientException.class)
                .hasMessage("stadium name match failed: 존재하지않는경기장");
    }

    @DisplayName("예외 : 홈팀을 찾을 수 없으면 예외가 발생한다")
    @Test
    void fetchGameList_homeTeamNotFound() {
        // given
        LocalDate today = TestFixture.getToday();
        KboGameItemDto gameItem = new KboGameItemDto(
                "20250721SSHH0", "2025-07-21", 0L, "18:30",
                "잠실", "존재하지않는원정팀", "삼성", "정상경기", "", "정규시즌", "5", "3"
        );
        KboClientResponse response = new KboClientResponse(List.of(gameItem), "100", "success");

        given(kboClient.fetchGame(today)).willReturn(response);

        // when & then
        assertThatThrownBy(() -> gameService.fetchGameList(today))
                .isInstanceOf(ClientException.class)
                .hasMessage("team code match failed: 존재하지않는원정팀");
    }

    @DisplayName("예외 : 원정팀을 찾을 수 없으면 예외가 발생한다")
    @Test
    void fetchGameList_awayTeamNotFound() {
        // given
        LocalDate today = TestFixture.getToday();
        KboGameItemDto gameItem = new KboGameItemDto(
                "20250721SSHH0", "2025-07-21", 0L, "18:30",
                "잠실", "한화", "존재하지않는원정팀", "정상경기", "", "정규시즌", "5", "3"
        );
        KboClientResponse response = new KboClientResponse(List.of(gameItem), "100", "success");

        given(kboClient.fetchGame(today)).willReturn(response);

        // when & then
        assertThatThrownBy(() -> gameService.fetchGameList(today))
                .isInstanceOf(ClientException.class)
                .hasMessage("team code match failed: 존재하지않는원정팀");
    }
}
