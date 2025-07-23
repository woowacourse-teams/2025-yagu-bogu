package com.yagubogu.stadium.domain;

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
@Table(name = "stadiums")
@Entity
public class Stadium {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stadium_id")
    private Long id;

    @Column(name = "full_name", nullable = false, unique = true)
    private String fullName;

    @Column(name = "shortName", nullable = false, unique = true)
    private String shortName;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    public Stadium(final String fullName, final String shortName, final String location, final Double latitude,
                   final Double longitude) {
        this.fullName = fullName;
        this.shortName = shortName;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
