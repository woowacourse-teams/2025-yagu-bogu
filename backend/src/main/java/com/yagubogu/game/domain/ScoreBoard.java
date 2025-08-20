package com.yagubogu.game.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "score_board")
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

    @Column(name = "inning_1_score", nullable = true)
    private Integer inning1Score;

    @Column(name = "inning_2_score", nullable = true)
    private Integer inning2Score;

    @Column(name = "inning_3_score", nullable = true)
    private Integer inning3Score;

    @Column(name = "inning_4_score", nullable = true)
    private Integer inning4Score;

    @Column(name = "inning_5_score", nullable = true)
    private Integer inning5Score;

    @Column(name = "inning_6_score", nullable = true)
    private Integer inning6Score;

    @Column(name = "inning_7_score", nullable = true)
    private Integer inning7Score;

    @Column(name = "inning_8_score", nullable = true)
    private Integer inning8Score;

    @Column(name = "inning_9_score", nullable = true)
    private Integer inning9Score;

    @Column(name = "inning_10_score", nullable = true)
    private Integer inning10Score;

    @Column(name = "inning_11_score", nullable = true)
    private Integer inning11Score;

    public ScoreBoard(final Integer runs, final Integer hits, final Integer errors, final Integer basesOnBalls,
                      final Integer inning1Score,
                      final Integer inning2Score, final Integer inning3Score, final Integer inning4Score,
                      final Integer inning5Score,
                      final Integer inning6Score, final Integer inning7Score, final Integer inning8Score,
                      final Integer inning9Score,
                      final Integer inning10Score, final Integer inning11Score
    ) {
        this.runs = runs;
        this.hits = hits;
        this.errors = errors;
        this.basesOnBalls = basesOnBalls;
        this.inning1Score = inning1Score;
        this.inning2Score = inning2Score;
        this.inning3Score = inning3Score;
        this.inning4Score = inning4Score;
        this.inning5Score = inning5Score;
        this.inning6Score = inning6Score;
        this.inning7Score = inning7Score;
        this.inning8Score = inning8Score;
        this.inning9Score = inning9Score;
        this.inning10Score = inning10Score;
        this.inning11Score = inning11Score;
    }

    public ScoreBoard(
            final Integer runs,
            final Integer hits,
            final Integer errors,
            final Integer basesOnBalls,
            final List<String> innings
    ) {
        this.runs = runs;
        this.hits = hits;
        this.errors = errors;
        this.basesOnBalls = basesOnBalls;
        this.inning1Score = parseScore(innings, 0);
        this.inning2Score = parseScore(innings, 1);
        this.inning3Score = parseScore(innings, 2);
        this.inning4Score = parseScore(innings, 3);
        this.inning5Score = parseScore(innings, 4);
        this.inning6Score = parseScore(innings, 5);
        this.inning7Score = parseScore(innings, 6);
        this.inning8Score = parseScore(innings, 7);
        this.inning9Score = parseScore(innings, 8);
        this.inning10Score = parseScore(innings, 9);
        this.inning11Score = parseScore(innings, 10);
    }

    private Integer parseScore(List<String> innings, int index) {
        if (innings == null || innings.size() <= index) {
            return null;
        }
        try {
            return Integer.valueOf(innings.get(index));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
