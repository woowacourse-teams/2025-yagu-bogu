package com.yagubogu.pastcheckin.service;

import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.BadRequestException;
import com.yagubogu.global.exception.ConflictException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.pastcheckin.domain.PastCheckIn;
import com.yagubogu.pastcheckin.dto.CreatePastCheckInRequest;
import com.yagubogu.pastcheckin.repository.PastCheckInRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.team.domain.Team;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PastCheckInService {

    private final PastCheckInRepository pastCheckInRepository;
    private final MemberRepository memberRepository;
    private final GameRepository gameRepository;
    private final CheckInRepository checkInRepository;

    @Transactional
    public void createPastCheckIn(final Long memberId, final CreatePastCheckInRequest request) {
        Game game = getGameById(request.gameId());
        Member member = getMember(memberId);
        Team team = member.getTeam();

        validateCheckInNotExists(member, game);

        validatePastCheckInNotExists(member, game);
        validatePast(request.date());

        PastCheckIn pastCheckIn = new PastCheckIn(game, member, team);
        pastCheckInRepository.save(pastCheckIn);
    }

    private void validatePast(final LocalDate date) {
        LocalDate now = LocalDate.now();
        if (date.isEqual(now) || date.isAfter(now)) {
            throw new BadRequestException("Past CheckIn should be add in past");
        }
    }

    private void validatePastCheckInNotExists(final Member member, final Game game) {
        boolean hasCheckIn = pastCheckInRepository.existsByMemberAndGameDate(member, game.getDate());
        if (hasCheckIn) {
            throw new ConflictException("CheckIn already exists");
        }
    }

    private void validateCheckInNotExists(final Member member, final Game game) {
        boolean hasCheckIn = checkInRepository.existsByMemberAndGameDate(member, game.getDate());
        if (hasCheckIn) {
            throw new ConflictException("CheckIn already exists");
        }
    }

    private Game getGameById(final long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game is not found"));
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
