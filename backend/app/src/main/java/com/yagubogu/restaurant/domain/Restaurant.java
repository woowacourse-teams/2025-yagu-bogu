package com.yagubogu.restaurant.domain;

import com.yagubogu.global.domain.BaseEntity;
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
@Table(name = "restaurants")
@Entity
public class Restaurant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_id")
    private Long id;

    @Column(name = "content_id", nullable = false)
    private String contentId;

    @Column(name = "stadium_id", nullable = false)
    private Long stadiumId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "address")
    private String address;

    @Column(name = "map_x", nullable = false)
    private Double mapX;

    @Column(name = "map_y", nullable = false)
    private Double mapY;

    @Column(name = "distance")
    private Integer distance;

    @Column(name = "tel")
    private String tel;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    public static Restaurant of(String contentId, Long stadiumId, String title, String address,
                                double mapX, double mapY, Integer distance, String tel, String imageUrl) {
        Restaurant r = new Restaurant();
        r.contentId = contentId;
        r.stadiumId = stadiumId;
        r.title = title;
        r.address = address;
        r.mapX = mapX;
        r.mapY = mapY;
        r.distance = distance;
        r.tel = tel;
        r.imageUrl = imageUrl;
        return r;
    }

    public void update(String title, String address, double mapX, double mapY,
                       Integer distance, String tel, String imageUrl) {
        this.title = title;
        this.address = address;
        this.mapX = mapX;
        this.mapY = mapY;
        this.distance = distance;
        this.tel = tel;
        this.imageUrl = imageUrl;
    }
}
