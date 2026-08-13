package com.yagubogu.data.repository.place

import com.yagubogu.data.dto.response.place.PlaceCategoryDto
import com.yagubogu.data.dto.response.place.PlacesResponse

interface PlaceRepository {
    suspend fun getPlaces(
        stadiumId: Long,
        category: PlaceCategoryDto,
    ): Result<PlacesResponse>
}
