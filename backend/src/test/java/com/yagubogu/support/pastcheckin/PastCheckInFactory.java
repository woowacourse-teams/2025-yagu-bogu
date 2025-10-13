package com.yagubogu.support.pastcheckin;

import com.yagubogu.pastcheckin.domain.PastCheckIn;
import com.yagubogu.pastcheckin.repository.PastCheckInRepository;
import java.util.function.Consumer;

public class PastCheckInFactory {

    private final PastCheckInRepository pastCheckInRepository;

    public PastCheckInFactory(final PastCheckInRepository pastCheckInRepository) {
        this.pastCheckInRepository = pastCheckInRepository;
    }

    public PastCheckIn save(final Consumer<PastCheckInBuilder> customizer) {
        PastCheckInBuilder builder = new PastCheckInBuilder();
        customizer.accept(builder);
        PastCheckIn pastCheckIn = builder.build();

        return pastCheckInRepository.save(pastCheckIn);
    }
}
