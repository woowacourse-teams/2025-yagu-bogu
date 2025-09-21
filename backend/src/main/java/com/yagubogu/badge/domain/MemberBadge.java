package com.yagubogu.badge.domain;

import com.yagubogu.global.domain.BaseEntity;
import com.yagubogu.member.domain.Member;
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

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "member_badges")
@Entity
public class MemberBadge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_badge_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "progress", nullable = false)
    private int progress = 0;

    @Column(name = "is_achieved", nullable = false)
    private boolean isAchieved = false;

    @Column(name = "achieved_at", nullable = true)
    private LocalDateTime achievedAt;

    public MemberBadge(final Badge badge, final Member member) {
        this.badge = badge;
        this.member = member;
    }

    public void increaseProgress(final int threshold) {
        if (isAchieved) {
            return;
        }
        progress++;
        checkUpdateAchieved(threshold);
    }

    private void checkUpdateAchieved(final int threshold) {
        if (progress >= threshold) {
            isAchieved = true;
            achievedAt = LocalDateTime.now();
        }
    }
}
