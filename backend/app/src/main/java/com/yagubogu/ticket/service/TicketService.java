package com.yagubogu.ticket.service;

import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.checkin.dto.StatCountsParam;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.ticket.dto.v1.TicketResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TicketService {

    private final CheckInRepository checkInRepository;

    public TicketResponse findTicket(final long memberId, final long checkInId) {
        CheckIn checkIn = checkInRepository.findByIdAndMemberId(checkInId, memberId)
                .orElseThrow(() -> new NotFoundException("CheckIn is not found"));

        int recordYear = checkIn.getGame().getDate().getYear();
        StatCountsParam statCounts = checkInRepository.findStatCountsByMemberAndTeam(
                checkIn.getMember(),
                checkIn.getTeam(),
                recordYear
        );
        double winRate = calculateWinRate(statCounts.winCounts(), statCounts.winCounts() + statCounts.loseCounts());

        return TicketResponse.from(checkIn, recordYear, statCounts, winRate);
    }

    private double calculateWinRate(final long winCounts, final long totalCountsWithoutDraw) {
        if (totalCountsWithoutDraw == 0) {
            return 0;
        }
        double rate = (double) winCounts / totalCountsWithoutDraw * 100;

        return Math.round(rate * 10) / 10.0;
    }
}
