package com.yagubogu.data.util

import okhttp3.Request

fun Request.addTokenHeader(accessToken: String): Request =
    this
        .newBuilder()
        .header("Authorization", "Bearer $accessToken")
        .build()

fun Request.getTokenFromHeader(): String? = this.header("Authorization")?.removePrefix("Bearer ")
