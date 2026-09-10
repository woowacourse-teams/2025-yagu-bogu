package com.yagubogu.widget.domain;

import com.yagubogu.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 디바이스별 푸시 토큰 등록 테이블.
 * deviceId는 앱이 생성한 UUID로, 자연 키로 사용됩니다.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "widget_devices")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class WidgetDevice {

    @Id
    @Column(name = "device_id")
    private String deviceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 10)
    private WidgetPlatform platform;

    @Column(name = "push_token", nullable = false, length = 500)
    private String pushToken;

    @Column(name = "app_version", length = 20)
    private String appVersion;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public WidgetDevice(final String deviceId, final Member member, final WidgetPlatform platform,
                        final String pushToken, final String appVersion) {
        this.deviceId = deviceId;
        this.member = member;
        this.platform = platform;
        this.pushToken = pushToken;
        this.appVersion = appVersion;
    }

    public void update(final Member member, final WidgetPlatform platform,
                       final String pushToken, final String appVersion) {
        this.member = member;
        this.platform = platform;
        this.pushToken = pushToken;
        this.appVersion = appVersion;
    }
}
