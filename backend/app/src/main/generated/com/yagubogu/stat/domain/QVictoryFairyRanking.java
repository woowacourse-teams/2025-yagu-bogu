package com.yagubogu.stat.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QVictoryFairyRanking is a Querydsl query type for VictoryFairyRanking
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QVictoryFairyRanking extends EntityPathBase<VictoryFairyRanking> {

    private static final long serialVersionUID = -1557947341L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QVictoryFairyRanking victoryFairyRanking = new QVictoryFairyRanking("victoryFairyRanking");

    public final NumberPath<Integer> checkInCount = createNumber("checkInCount", Integer.class);

    public final NumberPath<Integer> gameYear = createNumber("gameYear", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.yagubogu.member.domain.QMember member;

    public final NumberPath<Double> score = createNumber("score", Double.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> winCount = createNumber("winCount", Integer.class);

    public QVictoryFairyRanking(String variable) {
        this(VictoryFairyRanking.class, forVariable(variable), INITS);
    }

    public QVictoryFairyRanking(Path<? extends VictoryFairyRanking> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QVictoryFairyRanking(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QVictoryFairyRanking(PathMetadata metadata, PathInits inits) {
        this(VictoryFairyRanking.class, metadata, inits);
    }

    public QVictoryFairyRanking(Class<? extends VictoryFairyRanking> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.yagubogu.member.domain.QMember(forProperty("member"), inits.get("member")) : null;
    }

}

