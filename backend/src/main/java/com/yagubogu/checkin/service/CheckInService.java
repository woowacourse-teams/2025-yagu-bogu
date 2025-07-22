package com.yagubogu.checkin.service;

import com.yagubogu.checkin.repository.CheckInRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CheckInService {

    private final CheckInRepository checkInRepository;

}
