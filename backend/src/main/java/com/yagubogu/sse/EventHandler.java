package com.yagubogu.sse;

import com.yagubogu.checkin.dto.GameWithFanCountsResponse;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.sse.dto.CheckInCreatedEvent;
import com.yagubogu.sse.dto.GameWithFanRateResponse;
import com.yagubogu.sse.repository.SseEmitterRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@Component
public class EventHandler {

    private final SseEmitterRepository sseEmitterRepository;
    private final CheckInRepository checkInRepository;

    @Async
    @TransactionalEventListener
    public void onCheckInCreated(final CheckInCreatedEvent event) {
        List<GameWithFanRateResponse> eventData = buildCheckInEventData(event.date());

        sseEmitterRepository.all().forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("check-in-created")
                        .data(eventData));
            } catch (IOException e) {
                System.err.println("SSE 전송 실패: " + e.getMessage());
                log.warn("SSE 전송 실패: {}", e.getMessage(), e);
            }
        });
    }

    private List<GameWithFanRateResponse> buildCheckInEventData(final LocalDate date) {
        List<GameWithFanRateResponse> result = new ArrayList<>();

        List<GameWithFanCountsResponse> responses = checkInRepository.findGamesWithFanCountsByDate(date);
        for (GameWithFanCountsResponse response : responses) {
            Game game = response.game();
            long homeTeamCounts = response.homeTeamCheckInCounts();
            long awayTeamCounts = response.awayTeamCheckInCounts();
            long totalCounts = response.totalCheckInCounts();

            double homeTeamRate = calculateRoundRate(homeTeamCounts, totalCounts);
            double awayTeamRate = calculateRoundRate(awayTeamCounts, totalCounts);
            result.add(GameWithFanRateResponse.from(game, homeTeamRate, awayTeamRate));
        }

        return result;
    }

    private double calculateRoundRate(final Long checkInCounts, final Long total) {
        if (total == 0 || checkInCounts == 0) {
            return 0.0;
        }

        return Math.round(((double) checkInCounts / total) * 1000) / 10.0;
    }
}
