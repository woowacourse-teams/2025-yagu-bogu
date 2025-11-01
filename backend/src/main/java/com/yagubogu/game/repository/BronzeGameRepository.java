package com.yagubogu.game.repository;

import com.yagubogu.game.domain.BronzeGame;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BronzeGameRepository extends JpaRepository<BronzeGame, Long> {

    Optional<BronzeGame> findByDateAndStadiumAndHomeTeamAndAwayTeamAndStartTime(
            LocalDate date,
            String stadium,
            String homeTeam,
            String awayTeam,
            LocalTime startTime
    );

    @Query("SELECT b FROM BronzeGame b WHERE b.collectedAt >= :since ORDER BY b.collectedAt DESC")
    List<BronzeGame> findAllCollectedSince(@Param("since") LocalDateTime since);
}
