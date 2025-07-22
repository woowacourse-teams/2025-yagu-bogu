package com.yagubogu.stadium.service;

import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.stat.dto.OccupancyRateTotalResponse;
import com.yagubogu.stat.dto.OccupancyRateTotalResponse.OccupancyRateResponse;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StadiumService {

    private final GameRepository gameRepository;
    private final CheckInRepository checkInRepository;

    public OccupancyRateTotalResponse findOccupancyRate(final long stadiumId, final LocalDate today) {
        Game game = getGame(stadiumId, today);
        int checkInPeoples = checkInRepository.countByGame(game);

        return getOccupancyRateTotalResponse(game, checkInPeoples);
    }

    private Game getGame(final Long stadiumId, final LocalDate today) {
        return gameRepository.findByStadiumIdAndDate(stadiumId, today)
                .orElseThrow(() -> new NotFoundException("Game is not found"));
    }

    private OccupancyRateTotalResponse getOccupancyRateTotalResponse(final Game game, final int checkInPeoples) {
        List<Object[]> teamCheckIns = checkInRepository.countCheckInGroupByTeam(game);

        return new OccupancyRateTotalResponse(teamCheckIns.stream()
                .map(objects -> {
                    long teamId = (long) objects[0];
                    String teamName = (String) objects[1];
                    double occupancyRate = (1.0 * (Long) objects[2]) / checkInPeoples * 100;
                    double roundOccupancyRate = calculateRoundRate(occupancyRate);
                    return new OccupancyRateResponse(teamId, teamName, roundOccupancyRate);
                })
                .toList());
    }

    private double calculateRoundRate(final double rate) {
        return Math.round(rate * 10) / 10.0;
    }
}
