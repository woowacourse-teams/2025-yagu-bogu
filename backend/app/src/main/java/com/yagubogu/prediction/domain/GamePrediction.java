package com.yagubogu.prediction.domain;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.global.domain.BaseEntity;
import com.yagubogu.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "game_predictions")
@Entity
public class GamePrediction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(name = "pick", nullable = false)
    private PredictionPick pick;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PredictionStatus status;

    public GamePrediction(final Member member, final Game game, final PredictionPick pick) {
        this.member = member;
        this.game = game;
        this.pick = pick;
        this.status = PredictionStatus.SUBMITTED;
    }

    public void updatePick(final PredictionPick pick) {
        this.pick = pick;
    }

    public void applyResult(final Game game) {
        if (game.getGameState() == GameState.CANCELED) {
            this.status = PredictionStatus.VOID;
            return;
        }
        if (isWinningPick(game)) {
            this.status = PredictionStatus.WON;
            return;
        }
        this.status = PredictionStatus.LOST;
    }

    private boolean isWinningPick(final Game game) {
        if (pick == PredictionPick.HOME) {
            return game.isHomeWin();
        }
        return game.isAwayWin();
    }
}
