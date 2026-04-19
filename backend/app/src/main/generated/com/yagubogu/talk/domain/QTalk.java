package com.yagubogu.talk.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTalk is a Querydsl query type for Talk
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTalk extends EntityPathBase<Talk> {

    private static final long serialVersionUID = 1711492772L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTalk talk = new QTalk("talk");

    public final com.yagubogu.global.domain.QBaseEntity _super = new com.yagubogu.global.domain.QBaseEntity(this);

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final com.yagubogu.game.domain.QGame game;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.yagubogu.member.domain.QMember member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QTalk(String variable) {
        this(Talk.class, forVariable(variable), INITS);
    }

    public QTalk(Path<? extends Talk> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTalk(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTalk(PathMetadata metadata, PathInits inits) {
        this(Talk.class, metadata, inits);
    }

    public QTalk(Class<? extends Talk> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.game = inits.isInitialized("game") ? new com.yagubogu.game.domain.QGame(forProperty("game"), inits.get("game")) : null;
        this.member = inits.isInitialized("member") ? new com.yagubogu.member.domain.QMember(forProperty("member"), inits.get("member")) : null;
    }

}

