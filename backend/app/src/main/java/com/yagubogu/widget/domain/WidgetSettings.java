package com.yagubogu.widget.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 계정 단위 위젯 on/off 설정.
 * member_id를 PK로 사용하며 row가 없으면 enabled=true(기본값)으로 간주합니다.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "widget_settings")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class WidgetSettings {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public WidgetSettings(final Long memberId, final boolean enabled) {
        this.memberId = memberId;
        this.enabled = enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }
}
