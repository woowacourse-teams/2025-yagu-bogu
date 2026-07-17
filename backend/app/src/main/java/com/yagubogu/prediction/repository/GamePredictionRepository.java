package com.yagubogu.prediction.repository;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.member.domain.Member;
import com.yagubogu.prediction.domain.GamePrediction;
import com.yagubogu.prediction.domain.PredictionStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GamePredictionRepository extends JpaRepository<GamePrediction, Long> {

    Optional<GamePrediction> findByMemberAndGame(Member member, Game game);

    boolean existsByMemberAndGame(Member member, Game game);

    List<GamePrediction> findAllByGame(Game game);

    @Query("SELECT DISTINCT p.game FROM GamePrediction p "
            + "WHERE p.status = :status AND p.game.gameState IN :gameStates")
    List<Game> findGamesByPredictionStatusAndGameStateIn(
            @Param("status") PredictionStatus status,
            @Param("gameStates") Collection<GameState> gameStates
    );
}
