package com.yagubogu.game.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Embeddable
public class ScoreBoard {

    @Column(name = "runs", nullable = true)
    private Integer runs;

    @Column(name = "hits", nullable = true)
    private Integer hits;

    @Column(name = "errors", nullable = true)
    private Integer errors;

    @Column(name = "bases_on_balls", nullable = true)
    private Integer basesOnBalls;
}
