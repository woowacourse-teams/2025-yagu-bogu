package com.yagubogu.checkin.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.yagubogu.game.domain.QGame;
import com.yagubogu.pastcheckin.domain.QPastCheckIn;

@FunctionalInterface
public interface PastCheckInConditionBuilder {

    BooleanExpression build(QPastCheckIn pastCheckIn, QGame game);
}
