package com.yagubogu.support.checkin;

import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.checkin.repository.CheckInRepository;
import java.util.function.Consumer;

public class CheckInFactory {

    private final CheckInRepository checkInRepository;

    public CheckInFactory(final CheckInRepository checkInRepository) {
        this.checkInRepository = checkInRepository;
    }

    public CheckIn save(final Consumer<CheckInBuilder> customizer) {
        CheckInBuilder builder = new CheckInBuilder();
        customizer.accept(builder);
        CheckIn checkIn = builder.build();

        return checkInRepository.save(checkIn);
    }
}
