package com.yagubogu.badge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "badges")
@Entity
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "badge_id")
    private Long id;

    @Column(name = "badge_name", nullable = false)
    private String name;

    @Column(name = "badge_description", nullable = false)
    private String description;

    @Column(name = "badge_condition_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Policy type;

    @Column(name = "badge_condition_threshold", nullable = false)
    private Long threshold;

    public Badge(final String name, final String description, final Policy type, final Long threshold) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.threshold = threshold;
    }
}
