package com.yagubogu.support.game;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import java.util.function.Consumer;

public class GameFactory {

    private final GameRepository gameRepository;

    public GameFactory(final GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public Game save(final Consumer<GameBuilder> customizer) {
        GameBuilder builder = new GameBuilder();
        customizer.accept(builder);
        Game game = builder.build();

        return gameRepository.save(game);
    }
}
