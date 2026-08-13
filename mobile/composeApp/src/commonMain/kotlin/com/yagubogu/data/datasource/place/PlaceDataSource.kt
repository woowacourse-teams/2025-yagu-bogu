package com.yagubogu.data.datasource.place

import com.yagubogu.data.dto.response.place.PlaceCategoryDto
import com.yagubogu.data.dto.response.place.PlaceDetailResponse
import com.yagubogu.data.dto.response.place.PlacesResponse

interface PlaceDataSource {
    suspend fun getPlaces(
        stadiumId: Long,
        category: PlaceCategoryDto,
    ): Result<PlacesResponse>

    suspend fun getPlaceDetail(id: Long): Result<PlaceDetailResponse>
}
