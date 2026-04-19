package com.yagubogu.stadium.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStadium is a Querydsl query type for Stadium
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStadium extends EntityPathBase<Stadium> {

    private static final long serialVersionUID = 1554870260L;

    public static final QStadium stadium = new QStadium("stadium");

    public final StringPath fullName = createString("fullName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Double> latitude = createNumber("latitude", Double.class);

    public final EnumPath<StadiumLevel> level = createEnum("level", StadiumLevel.class);

    public final StringPath location = createString("location");

    public final NumberPath<Double> longitude = createNumber("longitude", Double.class);

    public final StringPath shortName = createString("shortName");

    public QStadium(String variable) {
        super(Stadium.class, forVariable(variable));
    }

    public QStadium(Path<? extends Stadium> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStadium(PathMetadata metadata) {
        super(Stadium.class, metadata);
    }

}

