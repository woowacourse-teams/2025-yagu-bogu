package com.yagubogu.stadium.service;

import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.domain.StadiumLevel;
import com.yagubogu.stadium.dto.StadiumsResponse;
import com.yagubogu.stadium.repository.StadiumRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class StadiumService {

    private final StadiumRepository stadiumRepository;

    public StadiumsResponse findAllMainStadiums() {
        List<Stadium> stadiums = stadiumRepository.findAllByLevel(StadiumLevel.MAIN);

        return StadiumsResponse.from(stadiums);
    }
}
