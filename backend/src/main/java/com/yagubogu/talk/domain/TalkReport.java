package com.yagubogu.talk.domain;

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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "talk_reports", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"talk_id", "reporter_id"})
})
@Entity
public class TalkReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "talk_report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "talk_id", nullable = false)
    private Talk talk;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    public TalkReport(final Talk talk, final Member reporter, final LocalDateTime reportedAt) {
        this.talk = talk;
        this.reporter = reporter;
        this.reportedAt = reportedAt;
    }
}
