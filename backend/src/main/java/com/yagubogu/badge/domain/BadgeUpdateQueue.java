package com.yagubogu.badge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "badge_update_queue")
@Entity
public class BadgeUpdateQueue {

    @Id
    @Column(name = "badge_update_queue_id")
    private Long id = 1L;

    @Column(name = "pending_count")
    private Long pendingCount = 0L;

    public void increment() {
        this.pendingCount += 1;
    }

    public void reset() {
        this.pendingCount = 0L;
    }
}
