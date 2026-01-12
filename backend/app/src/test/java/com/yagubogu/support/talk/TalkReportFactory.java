package com.yagubogu.support.talk;

import com.yagubogu.talk.domain.TalkReport;
import com.yagubogu.talk.repository.TalkReportRepository;
import java.util.function.Consumer;

public class TalkReportFactory {

    private final TalkReportRepository talkReportRepository;

    public TalkReportFactory(final TalkReportRepository talkReportRepository) {
        this.talkReportRepository = talkReportRepository;
    }

    public TalkReport save(final Consumer<TalkReportBuilder> customizer) {
        TalkReportBuilder builder = new TalkReportBuilder();
        customizer.accept(builder);
        TalkReport talkReport = builder.build();

        return talkReportRepository.save(talkReport);
    }
}

