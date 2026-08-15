package com.yagubogu.widget.domain;

import com.yagubogu.game.domain.Game;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
 * iOS Live Activity 갱신 토큰 등록 테이블 (iOS 전용).
 * activityId는 ActivityKit이 부여한 식별자로 자연 키로 사용됩니다.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "widget_live_activities")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class WidgetLiveActivity {

    @Id
    @Column(name = "activity_id")
    private String activityId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private WidgetDevice device;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "update_token", nullable = false, length = 500)
    private String updateToken;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public WidgetLiveActivity(final String activityId, final WidgetDevice device,
                              final Game game, final String updateToken) {
        this.activityId = activityId;
        this.device = device;
        this.game = game;
        this.updateToken = updateToken;
    }

    public void updateToken(final String updateToken) {
        this.updateToken = updateToken;
    }
}
