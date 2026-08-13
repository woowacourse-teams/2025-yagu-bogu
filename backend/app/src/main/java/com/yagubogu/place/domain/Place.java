package com.yagubogu.place.domain;

import com.yagubogu.global.domain.BaseEntity;
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
@Table(name = "places")
@Entity
public class Place extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long id;

    @Column(name = "content_id", nullable = false)
    private String contentId;

    @Column(name = "content_type_id", nullable = false)
    private Integer contentTypeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private PlaceCategory category;

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

    @Column(name = "detail_info", columnDefinition = "json")
    private String detailInfo;

    public static Place of(PlaceCategory category, String contentId, Long stadiumId, String title, String address,
                            double mapX, double mapY, Integer distance, String tel, String imageUrl) {
        Place place = new Place();
        place.category = category;
        place.contentTypeId = category.getContentTypeId();
        place.contentId = contentId;
        place.stadiumId = stadiumId;
        place.title = title;
        place.address = address;
        place.mapX = mapX;
        place.mapY = mapY;
        place.distance = distance;
        place.tel = tel;
        place.imageUrl = imageUrl;
        return place;
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

    public void updateDetailInfo(String detailInfo) {
        this.detailInfo = detailInfo;
    }

    public boolean hasDetailInfo() {
        return detailInfo != null;
    }
}
