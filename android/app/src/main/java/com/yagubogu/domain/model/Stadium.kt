package com.yagubogu.domain.model

enum class Stadium(
    val coordinate: Coordinate,
) {
    JAMSIL(Coordinate(Latitude(37.512192), Longitude(127.072055))),
    GOCHUK(Coordinate(Latitude(37.498191), Longitude(126.867073))),
    INCHEON(Coordinate(Latitude(37.437196), Longitude(126.693294))),
    SUWON(Coordinate(Latitude(37.299977), Longitude(127.009690))),
    DAEJEON(Coordinate(Latitude(36.316589), Longitude(127.431211))),
    GWANGJU(Coordinate(Latitude(35.168282), Longitude(126.889138))),
    DAEGU(Coordinate(Latitude(35.841318), Longitude(128.681559))),
    BUSAN(Coordinate(Latitude(35.194146), Longitude(129.061497))),
    CHANGWON(Coordinate(Latitude(35.222754), Longitude(128.582251))),
}
