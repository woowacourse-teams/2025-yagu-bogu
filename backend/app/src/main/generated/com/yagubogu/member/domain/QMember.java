package com.yagubogu.member.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = -968763392L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMember member = new QMember("member1");

    public final com.yagubogu.global.domain.QBaseEntity _super = new com.yagubogu.global.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final QNickname nickname;

    public final StringPath oauthId = createString("oauthId");

    public final EnumPath<OAuthProvider> provider = createEnum("provider", OAuthProvider.class);

    public final com.yagubogu.badge.domain.QBadge representativeBadge;

    public final EnumPath<Role> role = createEnum("role", Role.class);

    public final com.yagubogu.team.domain.QTeam team;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMember(String variable) {
        this(Member.class, forVariable(variable), INITS);
    }

    public QMember(Path<? extends Member> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMember(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMember(PathMetadata metadata, PathInits inits) {
        this(Member.class, metadata, inits);
    }

    public QMember(Class<? extends Member> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.nickname = inits.isInitialized("nickname") ? new QNickname(forProperty("nickname")) : null;
        this.representativeBadge = inits.isInitialized("representativeBadge") ? new com.yagubogu.badge.domain.QBadge(forProperty("representativeBadge")) : null;
        this.team = inits.isInitialized("team") ? new com.yagubogu.team.domain.QTeam(forProperty("team")) : null;
    }

}

