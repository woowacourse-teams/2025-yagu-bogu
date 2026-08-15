package com.yagubogu.data.datasource.place

import com.yagubogu.data.dto.response.place.PlaceCategoryDto
import com.yagubogu.data.dto.response.place.PlaceDetailResponse
import com.yagubogu.data.dto.response.place.PlacesResponse
import com.yagubogu.data.service.PlaceApiService
import com.yagubogu.data.util.safeApiCall

class PlaceRemoteDataSource(
    private val placeApiService: PlaceApiService,
) : PlaceDataSource {
    override suspend fun getPlaces(
        stadiumId: Long,
        category: PlaceCategoryDto,
    ): Result<PlacesResponse> =
        safeApiCall {
            placeApiService.getPlaces(stadiumId, category.name)
        }

    override suspend fun getPlaceDetail(id: Long): Result<PlaceDetailResponse> =
        safeApiCall {
            placeApiService.getPlaceDetail(id)
        }
}
