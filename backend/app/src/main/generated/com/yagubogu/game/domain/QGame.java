package com.yagubogu.game.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGame is a Querydsl query type for Game
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGame extends EntityPathBase<Game> {

    private static final long serialVersionUID = 1012974640L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGame game = new QGame("game");

    public final StringPath awayPitcher = createString("awayPitcher");

    public final NumberPath<Integer> awayScore = createNumber("awayScore", Integer.class);

    public final QScoreBoard awayScoreBoard;

    public final com.yagubogu.team.domain.QTeam awayTeam;

    public final DatePath<java.time.LocalDate> date = createDate("date", java.time.LocalDate.class);

    public final StringPath gameCode = createString("gameCode");

    public final EnumPath<GameState> gameState = createEnum("gameState", GameState.class);

    public final StringPath homePitcher = createString("homePitcher");

    public final NumberPath<Integer> homeScore = createNumber("homeScore", Integer.class);

    public final QScoreBoard homeScoreBoard;

    public final com.yagubogu.team.domain.QTeam homeTeam;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.yagubogu.stadium.domain.QStadium stadium;

    public final TimePath<java.time.LocalTime> startAt = createTime("startAt", java.time.LocalTime.class);

    public QGame(String variable) {
        this(Game.class, forVariable(variable), INITS);
    }

    public QGame(Path<? extends Game> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGame(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGame(PathMetadata metadata, PathInits inits) {
        this(Game.class, metadata, inits);
    }

    public QGame(Class<? extends Game> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.awayScoreBoard = inits.isInitialized("awayScoreBoard") ? new QScoreBoard(forProperty("awayScoreBoard")) : null;
        this.awayTeam = inits.isInitialized("awayTeam") ? new com.yagubogu.team.domain.QTeam(forProperty("awayTeam")) : null;
        this.homeScoreBoard = inits.isInitialized("homeScoreBoard") ? new QScoreBoard(forProperty("homeScoreBoard")) : null;
        this.homeTeam = inits.isInitialized("homeTeam") ? new com.yagubogu.team.domain.QTeam(forProperty("homeTeam")) : null;
        this.stadium = inits.isInitialized("stadium") ? new com.yagubogu.stadium.domain.QStadium(forProperty("stadium")) : null;
    }

}

