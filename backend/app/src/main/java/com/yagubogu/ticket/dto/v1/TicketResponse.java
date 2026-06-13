package com.yagubogu.ticket.dto.v1;

import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.checkin.dto.StatCountsParam;
import com.yagubogu.game.domain.Game;
import java.time.LocalDate;

public record TicketResponse(
        String homeTeamCode,
        String awayTeamCode,
        Integer homeScore,
        Integer awayScore,
        String myTeamCode,
        TicketRecordResponse record,
        String stadiumName,
        LocalDate attendanceDate
) {

    public static TicketResponse from(
            final CheckIn checkIn,
            final int recordYear,
            final StatCountsParam statCounts,
            final double winRate
    ) {
        Game game = checkIn.getGame();

        return new TicketResponse(
                game.getHomeTeam().getTeamCode(),
                game.getAwayTeam().getTeamCode(),
                game.getHomeScore(),
                game.getAwayScore(),
                checkIn.getTeam().getTeamCode(),
                TicketRecordResponse.from(recordYear, statCounts, winRate),
                game.getStadium().getFullName(),
                game.getDate()
        );
    }

    public record TicketRecordResponse(
            int year,
            int winCounts,
            int drawCounts,
            int loseCounts,
            double winRate,
            int checkInCounts
    ) {

        private static TicketRecordResponse from(
                final int year,
                final StatCountsParam statCounts,
                final double winRate
        ) {
            int checkInCounts = statCounts.winCounts() + statCounts.drawCounts() + statCounts.loseCounts();

            return new TicketRecordResponse(
                    year,
                    statCounts.winCounts(),
                    statCounts.drawCounts(),
                    statCounts.loseCounts(),
                    winRate,
                    checkInCounts
            );
        }
    }
}
