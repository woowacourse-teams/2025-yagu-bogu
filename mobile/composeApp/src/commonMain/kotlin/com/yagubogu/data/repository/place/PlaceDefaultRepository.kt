package com.yagubogu.data.repository.place

import com.yagubogu.data.datasource.place.PlaceDataSource
import com.yagubogu.data.dto.response.place.PlaceCategoryDto
import com.yagubogu.data.dto.response.place.PlacesResponse

class PlaceDefaultRepository(
    private val placeDataSource: PlaceDataSource,
) : PlaceRepository {
    override suspend fun getPlaces(
        stadiumId: Long,
        category: PlaceCategoryDto,
    ): Result<PlacesResponse> = placeDataSource.getPlaces(stadiumId, category)
}
