package com.yagubogu.game.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yagubogu.game.exception.GameSyncException;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GameStateTest {

    @DisplayName("KBO 경기 상태 문구를 명시적인 GameState로 변환한다")
    @ParameterizedTest
    @MethodSource("knownGameStates")
    void parseKnownGameState(final String status, final GameState expected) {
        assertThat(GameState.tryFromName(status)).contains(expected);
        assertThat(GameState.fromName(status)).isEqualTo(expected);
    }

    static Stream<Arguments> knownGameStates() {
        return Stream.of(
                Arguments.of("경기전", GameState.SCHEDULED),
                Arguments.of("경기예정", GameState.SCHEDULED),
                Arguments.of("경기중", GameState.LIVE),
                Arguments.of("3회초", GameState.LIVE),
                Arguments.of("우천중단", GameState.LIVE),
                Arguments.of("경기종료", GameState.COMPLETED),
                Arguments.of("경기취소", GameState.CANCELED)
        );
    }

    @DisplayName("알 수 없는 경기 상태는 LIVE로 추정하지 않는다")
    @ParameterizedTest
    @MethodSource("unknownGameStates")
    void doNotGuessUnknownStateAsLive(final String status) {
        assertThat(GameState.tryFromName(status)).isEmpty();
        assertThatThrownBy(() -> GameState.fromName(status))
                .isInstanceOf(GameSyncException.class)
                .hasMessageContaining("Unknown game status");
    }

    static Stream<String> unknownGameStates() {
        return Stream.of("상태확인중", "중계 준비", "-");
    }
}
