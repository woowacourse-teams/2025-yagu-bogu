package com.yagubogu.talk.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTalkReport is a Querydsl query type for TalkReport
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTalkReport extends EntityPathBase<TalkReport> {

    private static final long serialVersionUID = 855639288L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTalkReport talkReport = new QTalkReport("talkReport");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> reportedAt = createDateTime("reportedAt", java.time.LocalDateTime.class);

    public final com.yagubogu.member.domain.QMember reporter;

    public final QTalk talk;

    public QTalkReport(String variable) {
        this(TalkReport.class, forVariable(variable), INITS);
    }

    public QTalkReport(Path<? extends TalkReport> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTalkReport(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTalkReport(PathMetadata metadata, PathInits inits) {
        this(TalkReport.class, metadata, inits);
    }

    public QTalkReport(Class<? extends TalkReport> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.reporter = inits.isInitialized("reporter") ? new com.yagubogu.member.domain.QMember(forProperty("reporter"), inits.get("reporter")) : null;
        this.talk = inits.isInitialized("talk") ? new QTalk(forProperty("talk"), inits.get("talk")) : null;
    }

}

