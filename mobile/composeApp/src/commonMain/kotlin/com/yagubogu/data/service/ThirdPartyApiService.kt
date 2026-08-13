package com.yagubogu.data.service

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Url
import io.ktor.http.content.OutgoingContent

interface ThirdPartyApiService {
    @PUT
    suspend fun putImageToS3(
        @Url url: String,
        @Body body: OutgoingContent,
    )
}
