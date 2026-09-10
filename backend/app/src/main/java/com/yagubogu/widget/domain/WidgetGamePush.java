package com.yagubogu.widget.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 경기별 START/END 푸시 발송 이력.
 * 중복 START 방지 및 더블헤더 지연 시작 추적용.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "widget_game_pushes")
@Entity
public class WidgetGamePush {

    @Id
    @Column(name = "game_id")
    private Long gameId;

    @Column(name = "start_sent_at")
    private LocalDateTime startSentAt;

    @Column(name = "end_sent_at")
    private LocalDateTime endSentAt;

    public WidgetGamePush(final Long gameId) {
        this.gameId = gameId;
    }

    public boolean isStartSent() {
        return startSentAt != null;
    }

    public boolean isEndSent() {
        return endSentAt != null;
    }

    public void markStartSent(final LocalDateTime sentAt) {
        this.startSentAt = sentAt;
    }

    public void markEndSent(final LocalDateTime sentAt) {
        this.endSentAt = sentAt;
    }
}
