package com.yagubogu.checkin.service;

import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.checkin.domain.CheckInType;
import com.yagubogu.checkin.dto.CreatePastCheckInRequest;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.ConflictException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.team.domain.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PastCheckInService {

    private final MemberRepository memberRepository;
    private final GameRepository gameRepository;
    private final CheckInRepository checkInRepository;

    @Transactional
    public void createPastCheckIn(final Long memberId, final CreatePastCheckInRequest request) {
        Game game = getGameById(request.gameId());
        Member member = getMember(memberId);
        Team team = member.getTeam();

        validateCheckInNotExists(member, game);

        CheckIn pastCheckIn = new CheckIn(game, member, team, CheckInType.NON_LOCATION_CHECK_IN);
        checkInRepository.save(pastCheckIn);
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

    private Member getMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }
}
