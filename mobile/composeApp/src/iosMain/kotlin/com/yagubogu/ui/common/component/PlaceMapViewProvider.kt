package com.yagubogu.ui.common.component

import platform.UIKit.UIView
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "PlaceMapViewProvider")
object PlaceMapViewProvider {
    var create: ((address: String, placeName: String, latitude: Double, longitude: Double) -> UIView)? = null
    var update: ((view: UIView, address: String, placeName: String, latitude: Double, longitude: Double) -> Unit)? = null
    var dispose: ((view: UIView) -> Unit)? = null
}
