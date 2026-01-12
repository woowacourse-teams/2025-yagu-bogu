package com.yagubogu.support.talk;

import com.yagubogu.member.domain.Member;
import com.yagubogu.talk.domain.Talk;
import com.yagubogu.talk.domain.TalkReport;
import java.time.LocalDateTime;

public class TalkReportBuilder {

    private Talk talk;
    private Member reporter;
    private LocalDateTime reportedAt = LocalDateTime.now();

    public TalkReportBuilder talk(final Talk talk) {
        this.talk = talk;

        return this;
    }

    public TalkReportBuilder reporter(final Member reporter) {
        this.reporter = reporter;

        return this;
    }

    public TalkReportBuilder reportedAt(final LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;

        return this;
    }

    public TalkReport build() {
        return new TalkReport(talk, reporter, reportedAt);
    }
}
