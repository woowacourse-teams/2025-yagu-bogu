package com.yagubogu.game.dto;

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
public class Pitchers {
    private String winningPitcher;
    private String losingPitcher;
    private String savePitcher;
    private String holdPitcher;

    public void update(String winningPitcher, String losingPitcher, String savePitcher, String holdPitcher) {
        this.winningPitcher = winningPitcher;
        this.losingPitcher = losingPitcher;
        this.savePitcher = savePitcher;
        this.holdPitcher = holdPitcher;
    }
}
