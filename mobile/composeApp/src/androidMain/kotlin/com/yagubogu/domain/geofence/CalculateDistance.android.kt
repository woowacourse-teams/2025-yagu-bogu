package com.yagubogu.domain.geofence

import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Distance

actual fun calculateDistance(
    x: Coordinate,
    y: Coordinate,
): Distance {
    val result = FloatArray(1)
    android.location.Location.distanceBetween(
        x.latitude.value,
        x.longitude.value,
        y.latitude.value,
        y.longitude.value,
        result,
    )
    return Distance(result[0].toDouble())
}
