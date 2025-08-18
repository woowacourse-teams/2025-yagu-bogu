package com.yagubogu.game.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@AllArgsConstructor
@Getter
@Embeddable
public class ScoreBoardSummary {

    private Integer runs;
    private Integer hits;
    private Integer errors;
    private Integer basesOnBalls;
}
