package com.yagubogu.game.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QScoreBoard is a Querydsl query type for ScoreBoard
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QScoreBoard extends EntityPathBase<ScoreBoard> {

    private static final long serialVersionUID = 1887821106L;

    public static final QScoreBoard scoreBoard = new QScoreBoard("scoreBoard");

    public final NumberPath<Integer> basesOnBalls = createNumber("basesOnBalls", Integer.class);

    public final NumberPath<Integer> errors = createNumber("errors", Integer.class);

    public final NumberPath<Integer> hits = createNumber("hits", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<String, StringPath> inningScores = this.<String, StringPath>createList("inningScores", String.class, StringPath.class, PathInits.DIRECT2);

    public final NumberPath<Integer> runs = createNumber("runs", Integer.class);

    public QScoreBoard(String variable) {
        super(ScoreBoard.class, forVariable(variable));
    }

    public QScoreBoard(Path<? extends ScoreBoard> path) {
        super(path.getType(), path.getMetadata());
    }

    public QScoreBoard(PathMetadata metadata) {
        super(ScoreBoard.class, metadata);
    }

}

