package com.yagubogu.game.event;

import com.yagubogu.game.domain.GameState;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 경기 종료 이벤트
 *
 * 경기 상태가 COMPLETED나 CANCELED로 변경될 때 발행
 * RabbitMQ를 통해 크롤링 서버에서 백엔드 서버로 전달됨
 */
public record GameFinalizedEvent(
        LocalDate date,
        String stadium,
        String homeTeam,
        String awayTeam,
        LocalTime startTime,
        GameState state) implements Serializable {
}
