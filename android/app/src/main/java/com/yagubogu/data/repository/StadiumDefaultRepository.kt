package com.yagubogu.data.repository

import com.yagubogu.data.datasource.StadiumDataSource
import com.yagubogu.data.dto.response.StadiumDto
import com.yagubogu.data.dto.response.StadiumsResponse
import com.yagubogu.domain.model.Stadiums
import com.yagubogu.domain.repository.StadiumRepository

class StadiumDefaultRepository(
    private val stadiumDataSource: StadiumDataSource,
) : StadiumRepository {
    private var cachedStadiums: Stadiums? = null

    override suspend fun getStadiums(): Result<Stadiums> {
        cachedStadiums?.let { stadiums: Stadiums ->
            return Result.success(stadiums)
        }
        return stadiumDataSource
            .getStadiums()
            .map { stadiumsResponse: StadiumsResponse ->
                val stadiums =
                    Stadiums(
                        stadiumsResponse.stadiums.map { stadiumDto: StadiumDto ->
                            stadiumDto.toDomain()
                        },
                    )
                cachedStadiums = stadiums
                stadiums
            }
    }
}
