package com.yagubogu.team.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "teams")
@Entity
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "short_name", nullable = false, unique = true)
    private String shortName;

    public Team(final String name, final String shortName) {
        this.name = name;
        this.shortName = shortName;
    }
}
