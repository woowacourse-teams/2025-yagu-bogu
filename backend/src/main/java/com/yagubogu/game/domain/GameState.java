package com.yagubogu.game.domain;

import com.yagubogu.game.exception.GameSyncException;
import java.util.Arrays;
import java.util.List;

public enum GameState {

    SCHEDULED(1),
    LIVE(2),
    COMPLETED(3),
    CANCELED(4),
    ;

    private static final List<GameState> FINALIZED_GAME_STATES = List.of(COMPLETED, CANCELED);

    private final Integer stateNumber;

    GameState(final Integer stateNumber) {
        this.stateNumber = stateNumber;
    }

    public static GameState from(final Integer gameState) {
        return Arrays.stream(values())
                .filter(status -> status.stateNumber.equals(gameState))
                .findFirst()
                .orElseThrow(() -> new GameSyncException("Unknown game status: " + gameState));
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

    public static GameState fromStatus(String status) {
        if (status == null) {
            return SCHEDULED;
        }

        for (GameState state : values()) {
            if (state.name().equals(status)) {
                return state;
            }
        }

        return SCHEDULED;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isNotCompleted() {
        return !isCompleted();
    }

    public boolean isCanceled() {
        return this == CANCELED;
    }

    public boolean isFinalized() {
        return FINALIZED_GAME_STATES.contains(this);
    }
}
