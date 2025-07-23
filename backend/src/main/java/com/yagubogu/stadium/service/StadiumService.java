package com.yagubogu.stadium.service;

import com.yagubogu.checkin.dto.TeamCheckInCountResponse;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.stadium.dto.TeamOccupancyRatesResponse;
import com.yagubogu.stadium.dto.TeamOccupancyRatesResponse.TeamOccupancyRate;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StadiumService {

    private final GameRepository gameRepository;
    private final CheckInRepository checkInRepository;
    private final StadiumRepository stadiumRepository;

    public TeamOccupancyRatesResponse findOccupancyRate(final long stadiumId, final LocalDate date) {
        Game game = getGame(stadiumId, date);
        int checkInPeople = checkInRepository.countByGame(game);

        return getOccupancyRateTotalResponse(game, checkInPeople);
    }

    private Stadium getStadiumById(final long stadiumId) {
        return stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new NotFoundException("Stadium is not found"));
    }

    private Game getGame(final Stadium stadium, final LocalDate today) {
        return gameRepository.findByStadiumAndDate(stadium, today)
                .orElseThrow(() -> new NotFoundException("Game is not found"));
    }

    private TeamOccupancyRatesResponse getOccupancyRateTotalResponse(final Game game, final int checkInPeople) {
        List<TeamCheckInCountResponse> teamCheckIns = checkInRepository.countCheckInGroupByTeam(game);

        return new TeamOccupancyRatesResponse(teamCheckIns.stream()
                .map(response -> {
                    long teamId = response.id();
                    String teamName = response.name();
                    double occupancyRate = (1.0 * response.count()) / checkInPeople * 100;
                    double roundedOccupancyRate = calculateRoundRate(occupancyRate);
                    return new TeamOccupancyRate(teamId, teamName, roundedOccupancyRate);
                })
                .toList());
    }

    private double calculateRoundRate(final double rate) {
        return Math.round(rate * 10) / 10.0;
    }
}
