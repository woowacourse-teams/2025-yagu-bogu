package com.yagubogu.game.repository;

import com.yagubogu.game.domain.Game;
import com.yagubogu.stadium.domain.Stadium;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    Optional<Game> findByStadiumAndDate(Stadium stadium, LocalDate date);

    Optional<Game> findByGameCode(String gameCode);
}
