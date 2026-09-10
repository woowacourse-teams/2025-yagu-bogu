package com.yagubogu.widget.domain;

import com.yagubogu.game.domain.Game;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "widget_live_activities")
@Entity
public class WidgetLiveActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "widget_live_activity_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "widget_device_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private WidgetDevice device;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "activity_id", nullable = false, unique = true, length = 255)
    private String activityId;

    @Column(name = "update_token", nullable = false, length = 4096)
    private String updateToken;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public WidgetLiveActivity(
            final WidgetDevice device,
            final Game game,
            final String activityId,
            final String updateToken
    ) {
        this.device = device;
        this.game = game;
        this.activityId = activityId;
        this.updateToken = updateToken;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void update(final String activityId, final String updateToken) {
        this.activityId = activityId;
        this.updateToken = updateToken;
        this.updatedAt = LocalDateTime.now();
    }
}
