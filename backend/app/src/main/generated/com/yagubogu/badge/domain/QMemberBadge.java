package com.yagubogu.badge.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMemberBadge is a Querydsl query type for MemberBadge
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberBadge extends EntityPathBase<MemberBadge> {

    private static final long serialVersionUID = 334815290L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMemberBadge memberBadge = new QMemberBadge("memberBadge");

    public final com.yagubogu.global.domain.QBaseEntity _super = new com.yagubogu.global.domain.QBaseEntity(this);

    public final DateTimePath<java.time.LocalDateTime> achievedAt = createDateTime("achievedAt", java.time.LocalDateTime.class);

    public final QBadge badge;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isAchieved = createBoolean("isAchieved");

    public final com.yagubogu.member.domain.QMember member;

    public final NumberPath<Integer> progress = createNumber("progress", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMemberBadge(String variable) {
        this(MemberBadge.class, forVariable(variable), INITS);
    }

    public QMemberBadge(Path<? extends MemberBadge> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMemberBadge(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMemberBadge(PathMetadata metadata, PathInits inits) {
        this(MemberBadge.class, metadata, inits);
    }

    public QMemberBadge(Class<? extends MemberBadge> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.badge = inits.isInitialized("badge") ? new QBadge(forProperty("badge")) : null;
        this.member = inits.isInitialized("member") ? new com.yagubogu.member.domain.QMember(forProperty("member"), inits.get("member")) : null;
    }

}

