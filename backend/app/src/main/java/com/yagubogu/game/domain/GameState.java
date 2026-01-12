package com.yagubogu.game.domain;

import com.yagubogu.game.exception.GameSyncException;
import java.util.Arrays;
import java.util.List;

public enum GameState {

    SCHEDULED(1, "경기전"),
    LIVE(2, "중"),
    COMPLETED(3, "경기종료"),
    CANCELED(4, "경기취소"),
    ;

    private static final List<GameState> FINALIZED_GAME_STATES = List.of(COMPLETED, CANCELED);

    private final Integer stateNumber;
    private final String statusName;

    GameState(final Integer stateNumber, final String statusName) {
        this.stateNumber = stateNumber;
        this.statusName = statusName;
    }

    public static GameState fromNumber(final Integer gameState) {
        return Arrays.stream(values())
                .filter(status -> status.stateNumber.equals(gameState))
                .findFirst()
                .orElseThrow(() -> new GameSyncException("Unknown game status: " + gameState));
    }

    public static GameState fromName(final String state) {
        if (state == null || state.isEmpty()) {
            return GameState.SCHEDULED;
        }

        return Arrays.stream(values())
                .filter(status -> state.contains(status.statusName))
                .findFirst()
                .orElse(LIVE);
    }

    public boolean canTransitionTo(GameState newState) {
        if (this.isFinalized()) {
            return false;
        }

        return switch (this) {
            case SCHEDULED -> newState == LIVE || newState == CANCELED;
            case LIVE -> newState == COMPLETED || newState == CANCELED;
            default -> false;
        };
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isCanceled() {
        return this == CANCELED;
    }

    public boolean isFinalized() {
        return FINALIZED_GAME_STATES.contains(this);
    }
}
