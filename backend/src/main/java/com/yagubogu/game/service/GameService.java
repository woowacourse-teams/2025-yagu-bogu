package com.yagubogu.game.service;

import com.yagubogu.game.dto.GameResponse;
import com.yagubogu.game.dto.GameWithCheckIn;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.global.exception.UnprocessableEntityException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GameService {

    private final GameRepository gameRepository;
    private final MemberRepository memberRepository;

    public GameResponse findGamesByDate(final LocalDate date, final long memberId) {
        Member member = getMember(memberId);
        validateIsNotFuture(date);

        List<GameWithCheckIn> gameWithCheckIns = gameRepository.findGamesWithCheckInsByDate(date, member);

        return new GameResponse(gameWithCheckIns);
    }

    private Member getMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }

    private void validateIsNotFuture(final LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            throw new UnprocessableEntityException("Cannot retrieve games for future dates");
        }
    }
}
