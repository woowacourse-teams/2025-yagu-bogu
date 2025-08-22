package com.yagubogu.game.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "score_boards")
@Entity
public class ScoreBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "score_board_id")
    private Long id;

    @Column(name = "runs", nullable = false)
    private Integer runs = 0;

    @Column(name = "hits", nullable = false)
    private Integer hits = 0;

    @Column(name = "errors", nullable = false)
    private Integer errors = 0;

    @Column(name = "bases_on_balls", nullable = false)
    private Integer basesOnBalls = 0;

    @Column(name = "inning_scores", nullable = false, length = 100)
    @Convert(converter = InningScoreConverter.class)
    private List<String> inningScores = new ArrayList<>();

    public ScoreBoard(
            final Integer runs,
            final Integer hits,
            final Integer errors,
            final Integer basesOnBalls,
            final List<String> inningScores

    ) {
        this.runs = runs;
        this.hits = hits;
        this.errors = errors;
        this.basesOnBalls = basesOnBalls;
        this.inningScores = new ArrayList<>(inningScores);
    }
}
