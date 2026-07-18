package com.yagubogu.ui.common.component

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import platform.UIKit.UIView

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "PlaceMapViewProvider")
object PlaceMapViewProvider {
    var create: ((address: String, placeName: String, latitude: Double, longitude: Double) -> UIView)? = null
    var update: ((view: UIView, address: String, placeName: String, latitude: Double, longitude: Double) -> Unit)? = null
    var dispose: ((view: UIView) -> Unit)? = null
}
