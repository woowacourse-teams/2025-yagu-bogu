package com.yagubogu.common

import timber.log.Timber

class YaguBoguDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String = "${element.fileName}:${element.lineNumber}"
}
