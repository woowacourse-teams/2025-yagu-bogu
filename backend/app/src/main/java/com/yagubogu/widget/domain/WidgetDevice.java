package com.yagubogu.widget.domain;

import com.yagubogu.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "widget_devices")
@Entity
public class WidgetDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "widget_device_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private WidgetPlatform platform;

    @Column(name = "device_id", nullable = false, unique = true, length = 36)
    private String deviceId;

    @Column(name = "push_token", nullable = false, length = 4096)
    private String pushToken;

    @Column(name = "app_version", length = 50)
    private String appVersion;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public WidgetDevice(
            final Member member,
            final WidgetPlatform platform,
            final String deviceId,
            final String pushToken,
            final String appVersion
    ) {
        this.member = member;
        this.platform = platform;
        this.deviceId = deviceId;
        this.pushToken = pushToken;
        this.appVersion = appVersion;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void updateRegistration(
            final Member member,
            final WidgetPlatform platform,
            final String pushToken,
            final String appVersion
    ) {
        this.member = member;
        this.platform = platform;
        this.pushToken = pushToken;
        this.appVersion = appVersion;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean belongsTo(final long memberId) {
        return member.isSameId(memberId);
    }

    public boolean isIos() {
        return platform == WidgetPlatform.IOS;
    }

    public void updateEnabled(final boolean enabled) {
        this.enabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }
}
