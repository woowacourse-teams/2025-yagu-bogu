package com.yagubogu.presentation.util

inline fun <T, R> Result<List<T>>.mapList(transform: (T) -> R): Result<List<R>> = this.map { list: List<T> -> list.map(transform) }
