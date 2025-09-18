package com.yagubogu.like.domain;

import com.yagubogu.game.domain.Game;
import com.yagubogu.team.domain.Team;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "likes")
@Entity
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "total_count", nullable = false)
    private long totalCount = 0L;

    public Like(final Game game, final Team team) {
        this.game = game;
        this.team = team;
    }

    public static Like of(Game game, Team team) {
        return new Like(game, team);
    }

    public void add(long delta) {
        if (delta == 0) {
            return;
        }
        this.totalCount = this.totalCount + delta;
    }
}
