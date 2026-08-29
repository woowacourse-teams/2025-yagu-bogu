package com.yagubogu.place.dto.v1.detail;

public sealed interface PlaceDetail
        permits AttractionDetail, PerformanceDetail, LodgingDetail, FoodDetail {
}
