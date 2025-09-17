package com.yagubogu.support.talk;

import com.yagubogu.talk.domain.Talk;
import com.yagubogu.talk.repository.TalkRepository;
import java.util.function.Consumer;

public class TalkFactory {

    private final TalkRepository talkRepository;

    public TalkFactory(final TalkRepository talkRepository) {
        this.talkRepository = talkRepository;
    }

    public Talk save(final Consumer<TalkBuilder> customizer) {
        TalkBuilder builder = new TalkBuilder();
        customizer.accept(builder);
        Talk talk = builder.build();

        return talkRepository.save(talk);
    }
}
