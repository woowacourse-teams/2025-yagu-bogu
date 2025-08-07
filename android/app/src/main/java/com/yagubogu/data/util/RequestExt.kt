package com.yagubogu.data.util

import okhttp3.Request

fun Request.addTokenHeader(accessToken: String): Request =
    this
        .newBuilder()
        .addHeader("Authorization", "Bearer $accessToken")
        .build()
