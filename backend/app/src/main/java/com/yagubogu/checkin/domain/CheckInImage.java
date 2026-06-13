package com.yagubogu.checkin.domain;

import com.yagubogu.global.domain.BaseEntity;
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

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "check_in_images")
@Entity
public class CheckInImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_in_images_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "check_in_id", nullable = false)
    private CheckIn checkIn;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "image_key", length = 500)
    private String imageKey;

    public CheckInImage(final CheckIn checkIn, final String imageUrl) {
        this.checkIn = checkIn;
        this.imageUrl = imageUrl;
    }

    private CheckInImage(final CheckIn checkIn, final String imageUrl, final String imageKey) {
        this.checkIn = checkIn;
        this.imageUrl = imageUrl;
        this.imageKey = imageKey;
    }

    public static CheckInImage privateImage(final CheckIn checkIn, final String imageKey) {
        return new CheckInImage(checkIn, null, imageKey);
    }

    public boolean hasImageKey() {
        return imageKey != null && !imageKey.isBlank();
    }
}
