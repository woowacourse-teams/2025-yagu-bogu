package com.yagubogu.checkin.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCheckIn is a Querydsl query type for CheckIn
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCheckIn extends EntityPathBase<CheckIn> {

    private static final long serialVersionUID = 1847363092L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCheckIn checkIn = new QCheckIn("checkIn");

    public final com.yagubogu.global.domain.QBaseEntity _super = new com.yagubogu.global.domain.QBaseEntity(this);

    public final EnumPath<CheckInType> checkInType = createEnum("checkInType", CheckInType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final com.yagubogu.game.domain.QGame game;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.yagubogu.member.domain.QMember member;

    public final com.yagubogu.team.domain.QTeam team;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QCheckIn(String variable) {
        this(CheckIn.class, forVariable(variable), INITS);
    }

    public QCheckIn(Path<? extends CheckIn> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCheckIn(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCheckIn(PathMetadata metadata, PathInits inits) {
        this(CheckIn.class, metadata, inits);
    }

    public QCheckIn(Class<? extends CheckIn> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.game = inits.isInitialized("game") ? new com.yagubogu.game.domain.QGame(forProperty("game"), inits.get("game")) : null;
        this.member = inits.isInitialized("member") ? new com.yagubogu.member.domain.QMember(forProperty("member"), inits.get("member")) : null;
        this.team = inits.isInitialized("team") ? new com.yagubogu.team.domain.QTeam(forProperty("team")) : null;
    }

}

