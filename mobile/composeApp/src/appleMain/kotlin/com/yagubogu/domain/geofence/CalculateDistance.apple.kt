package com.yagubogu.domain.geofence

import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Distance
import platform.CoreLocation.CLLocation

actual fun calculateDistance(
    x: Coordinate,
    y: Coordinate,
): Distance {
    val location1 = CLLocation(x.latitude.value, x.longitude.value)
    val location2 = CLLocation(y.latitude.value, y.longitude.value)
    return Distance(location1.distanceFromLocation(location2))
}
