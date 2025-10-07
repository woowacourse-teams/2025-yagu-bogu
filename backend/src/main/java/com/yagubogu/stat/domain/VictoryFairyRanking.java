package com.yagubogu.stadium.domain;

import com.yagubogu.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "victory_fairy_rankings")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class VictoryFairyRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "victory_fairy_ranking_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "score", nullable = false)
    private double score = 0.0;

    @Column(name = "win_count", nullable = false)
    private int winCount = 0;

    @Column(name = "check_in_count", nullable = false)
    private int checkInCount = 0;

    @Column(name = "game_year", nullable = false)
    private int gameYear;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime updatedAt;

    public VictoryFairyRanking(
            final Long id,
            final Member member,
            final double score,
            final int winCount,
            final int checkInCount,
            final int gameYear,
            final LocalDateTime updatedAt
    ) {
        this.id = id;
        this.member = member;
        this.score = score;
        this.winCount = winCount;
        this.checkInCount = checkInCount;
        this.gameYear = gameYear;
        this.updatedAt = updatedAt;
    }
}
