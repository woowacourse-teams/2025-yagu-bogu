package com.yagubogu.stadium.service;

import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.dto.StadiumsResponse;
import com.yagubogu.stadium.repository.StadiumRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class StadiumService {

    private final StadiumRepository stadiumRepository;

    public StadiumsResponse findStadiumsWithGame() {
        List<Stadium> stadiums = stadiumRepository.findStadiumsByGameDate(LocalDate.now());

        return StadiumsResponse.from(stadiums);
    }
}
