package com.yagubogu.stadium.service;

import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.stat.dto.OccupancyRateTotalResponse;
import com.yagubogu.stat.dto.OccupancyRateTotalResponse.OccupancyRateResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StadiumService {

    private final GameRepository gameRepository;
    private final CheckInRepository checkInRepository;
    private final StadiumRepository stadiumRepository;

    public OccupancyRateTotalResponse findOccupancyRate(final long stadiumId, final LocalDate today) {
        List<Stadium> all = stadiumRepository.findAll();
        Stadium stadium = stadiumRepository.findById(stadiumId).orElseThrow();
        Game game = getGame(stadium, today);
        int checkInPeoples = checkInRepository.countByGame(game);

        List<OccupancyRateResponse> responses = new ArrayList<>();
        List<Object[]> teamCheckIns = checkInRepository.countCheckInGroupByTeam(game);
        for (Object[] teamCheckIn : teamCheckIns) {
            long teamId = (long) teamCheckIn[0];
            String teamName = (String) teamCheckIn[1];
            double occupancyRate = (1.0 * (int) teamCheckIn[2]) / checkInPeoples;
            double roundOccupancyRate = calculateRoundRate(occupancyRate);
            responses.add(new OccupancyRateResponse(teamId, teamName, roundOccupancyRate));
        }

        return new OccupancyRateTotalResponse(responses);
    }

    private Game getGame(final Stadium stadium, final LocalDate today) {
        List<Game> games = gameRepository.findAll();
        Optional<Game> gameById = gameRepository.findById(1L);
        Optional<Game> byStadiumId1 = gameRepository.findByStadium_Id(1L);

        Optional<Game> byStadiumId = gameRepository.findByStadium_Id(1L);
        return gameRepository.findByStadiumAndDate(stadium, today)
                .orElseThrow(() -> new NotFoundException("Game is not found"));
    }

    private double calculateRoundRate(final double rate) {
        return Math.round(rate * 10) / 10.0;
    }
}
