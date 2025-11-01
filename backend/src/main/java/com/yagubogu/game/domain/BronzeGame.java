package com.yagubogu.game.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "bronze_games_raw",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_natural_key",
                columnNames = {"date", "stadium", "home_team", "away_team", "start_time"}
        ))
@Entity
public class BronzeGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "raw_id")
    private Long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "stadium", nullable = false, length = 50)
    private String stadium;

    @Column(name = "home_team", nullable = false, length = 50)
    private String homeTeam;

    @Column(name = "away_team", nullable = false, length = 50)
    private String awayTeam;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "collected_at", nullable = false)
    private LocalDateTime collectedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "json")
    private String payload;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    public BronzeGame(final LocalDate date,
                      final String stadium,
                      final String homeTeam,
                      final String awayTeam,
                      final LocalTime startTime,
                      final LocalDateTime collectedAt,
                      final String payload,
                      final String contentHash) {
        this.date = date;
        this.stadium = stadium;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.startTime = startTime;
        this.collectedAt = collectedAt;
        this.payload = payload;
        this.contentHash = contentHash;
    }

    public void update(final LocalDateTime collectedAt, final String payload, final String contentHash) {
        this.collectedAt = collectedAt;
        this.payload = payload;
        this.contentHash = contentHash;
    }
}
