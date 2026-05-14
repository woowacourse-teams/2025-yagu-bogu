package com.yagubogu.ui.util

inline fun <T, R> Result<List<T>>.mapList(transform: (T) -> R): Result<List<R>> =
    this.mapCatching { list: List<T> ->
        list.map(transform)
    }
