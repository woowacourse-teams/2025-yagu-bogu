package com.yagubogu.data.service

import com.yagubogu.data.dto.response.place.PlacesResponse
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query

interface PlaceApiService {
    @GET("/api/v1/places")
    suspend fun getPlaces(
        @Query("stadiumId") stadiumId: Long,
        @Query("category") category: String,
    ): PlacesResponse
}
