package com.yagubogu.data.repository

import com.yagubogu.data.datasource.StadiumRemoteDataSource
import com.yagubogu.data.dto.response.StadiumDto
import com.yagubogu.data.dto.response.StadiumsResponse
import com.yagubogu.domain.model.Stadiums
import com.yagubogu.domain.repository.StadiumRepository

class StadiumDefaultRepository(
    private val stadiumRemoteDataSource: StadiumRemoteDataSource,
) : StadiumRepository {
    private var cachedStadiums: Stadiums? = null

    override suspend fun getStadiums(): Stadiums {
        cachedStadiums?.let { stadiums: Stadiums ->
            return stadiums
        }

        val stadiumsResponse: StadiumsResponse = stadiumRemoteDataSource.getStadiums()
        val stadiums =
            Stadiums(
                stadiumsResponse.stadiums.map { stadiumDto: StadiumDto ->
                    stadiumDto.toDomain()
                },
            )
        cachedStadiums = stadiums
        return stadiums
    }
}
