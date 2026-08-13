package com.yagubogu.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

internal fun createDataStore(
    context: Context,
    fileName: String,
): DataStore<Preferences> =
    createDataStore(
        producePath = { context.filesDir.resolve("datastore/$fileName.preferences_pb").absolutePath },
    )
