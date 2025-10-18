package com.yagubogu.checkin.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.yagubogu.checkin.domain.QCheckIn;
import com.yagubogu.game.domain.QGame;

@FunctionalInterface
public interface CheckInConditionBuilder {

    BooleanExpression build(QCheckIn checkIn, QGame game);
}
