package com.yagubogu.leaderboard.service;

import com.yagubogu.leaderboard.domain.LeaderboardType;
import com.yagubogu.leaderboard.dto.LeaderboardRow;
import com.yagubogu.talk.repository.TalkRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChattiestLeaderboardQuery implements LeaderboardQuery {

    private final TalkRepository talkRepository;

    @Override
    public LeaderboardType supports() {
        return LeaderboardType.CHATTIEST;
    }

    @Override
    public List<LeaderboardRow> findTop(final int limit) {
        return talkRepository.findChattiestWinner(limit);
    }
}
