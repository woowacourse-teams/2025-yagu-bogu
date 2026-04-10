package com.yagubogu.domain.geofence

import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Distance

expect fun calculateDistance(
    x: Coordinate,
    y: Coordinate,
): Distance
