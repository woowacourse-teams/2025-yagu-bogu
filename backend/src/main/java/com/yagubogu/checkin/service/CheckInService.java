package com.yagubogu.checkin.service;

import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.checkin.dto.CheckInCountsResponse;
import com.yagubogu.checkin.dto.CreateCheckInRequest;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final MemberRepository memberRepository;
    private final StadiumRepository stadiumRepository;
    private final GameRepository gameRepository;

    public void createCheckIn(final CreateCheckInRequest request) {
        long stadiumId = request.stadiumId();
        Stadium stadium = getStadiumById(stadiumId);
        LocalDate date = request.date();
        Game game = getGame(stadium, date);

        long memberId = request.memberId();
        Member member = getMember(memberId);

        checkInRepository.save(new CheckIn(game, member));
    }

    public CheckInCountsResponse findCheckInCounts(final long memberId, final long year) {
        Member member = getMember(memberId);
        int checkInCounts = checkInRepository.countByMemberAndYear(member, year);

        return new CheckInCountsResponse(checkInCounts);
    }

    private Stadium getStadiumById(final long stadiumId) {
        return stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new NotFoundException("Stadium is not found"));
    }

    private Game getGame(final Stadium stadium, final LocalDate date) {
        return gameRepository.findByStadiumAndDate(stadium, date)
                .orElseThrow(() -> new NotFoundException("Game is not found"));
    }

    private Member getMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }
}
