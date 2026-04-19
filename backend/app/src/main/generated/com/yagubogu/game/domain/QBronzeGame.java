package com.yagubogu.game.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBronzeGame is a Querydsl query type for BronzeGame
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBronzeGame extends EntityPathBase<BronzeGame> {

    private static final long serialVersionUID = 968866410L;

    public static final QBronzeGame bronzeGame = new QBronzeGame("bronzeGame");

    public final StringPath awayTeam = createString("awayTeam");

    public final DateTimePath<java.time.LocalDateTime> collectedAt = createDateTime("collectedAt", java.time.LocalDateTime.class);

    public final StringPath contentHash = createString("contentHash");

    public final DatePath<java.time.LocalDate> date = createDate("date", java.time.LocalDate.class);

    public final DateTimePath<java.time.LocalDateTime> etlProcessedAt = createDateTime("etlProcessedAt", java.time.LocalDateTime.class);

    public final StringPath homeTeam = createString("homeTeam");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath payload = createString("payload");

    public final StringPath stadium = createString("stadium");

    public final TimePath<java.time.LocalTime> startTime = createTime("startTime", java.time.LocalTime.class);

    public final EnumPath<GameState> state = createEnum("state", GameState.class);

    public QBronzeGame(String variable) {
        super(BronzeGame.class, forVariable(variable));
    }

    public QBronzeGame(Path<? extends BronzeGame> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBronzeGame(PathMetadata metadata) {
        super(BronzeGame.class, metadata);
    }

}

