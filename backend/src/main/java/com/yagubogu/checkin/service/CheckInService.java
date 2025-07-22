package com.yagubogu.checkin.service;

import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.checkin.dto.CreateCheckInRequest;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
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
        validateStadium(stadiumId);
        LocalDate date = request.date();
        Game game = getGame(stadiumId, date);

        long memberId = request.memberId();
        Member member = getMember(memberId);

        checkInRepository.save(new CheckIn(game, member));
    }

    private void validateStadium(final long stadiumId) {
        stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new NotFoundException("Stadium is not found"));
    }

    private Game getGame(final long stadiumId, final LocalDate date) {
        return gameRepository.findByStadiumIdAndDate(stadiumId, date)
                .orElseThrow(() -> new NotFoundException("Game is not found"));
    }

    private Member getMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }
}
