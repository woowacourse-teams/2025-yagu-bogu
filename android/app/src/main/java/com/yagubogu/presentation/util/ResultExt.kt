package com.yagubogu.presentation.util

inline fun <T, R> Result<List<T>>.mapList(transform: (T) -> R): Result<List<R>> =
    this.map { list: List<T> ->
        list.map(transform)
    }

inline fun <T, R> Result<List<T>>.mapListIndexed(transform: (index: Int, T) -> R): Result<List<R>> =
    this.map { list: List<T> ->
        list.mapIndexed { index: Int, element: T ->
            transform(index, element)
        }
    }
