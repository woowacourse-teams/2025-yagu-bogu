package com.yagubogu.stadium.service;

import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.dto.StadiumsResponse;
import com.yagubogu.stadium.repository.StadiumRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StadiumService {

    private final StadiumRepository stadiumRepository;

    public StadiumsResponse findAll() {
        List<Stadium> stadiums = stadiumRepository.findAll();

        return StadiumsResponse.from(stadiums);
    }
}
