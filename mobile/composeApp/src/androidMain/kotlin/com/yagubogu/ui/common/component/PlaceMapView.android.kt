package com.yagubogu.ui.common.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.TypedValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kakao.vectormap.GestureType
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.yagubogu.BuildKonfig

private const val PLACE_MAP_ZOOM_LEVEL = 16

@Composable
actual fun PlaceMapView(
    address: String,
    placeName: String,
    latitude: Double,
    longitude: Double,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val position = remember(latitude, longitude) { LatLng.from(latitude, longitude) }
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    LaunchedEffect(position, kakaoMap) {
        val map = kakaoMap ?: return@LaunchedEffect
        val markerStyles = createPlaceMarkerStyles(context, map) ?: return@LaunchedEffect
        map.moveCamera(CameraUpdateFactory.newCenterPosition(position, PLACE_MAP_ZOOM_LEVEL))
        map.labelManager?.layer?.apply {
            removeAll()
            addLabel(LabelOptions.from(position).setStyles(markerStyles))
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            if (!KakaoMapSdk.isInitialized() && BuildKonfig.KAKAO_MAP_API.isNotBlank()) {
                KakaoMapSdk.init(context.applicationContext, BuildKonfig.KAKAO_MAP_API)
            }

            MapView(context).also { newMapView ->
                mapView = newMapView
                newMapView.start(
                    object : MapLifeCycleCallback() {
                        override fun onMapDestroy() = Unit

                        override fun onMapError(error: Exception) = Unit
                    },
                    object : KakaoMapReadyCallback() {
                        override fun getPosition(): LatLng = position

                        override fun getZoomLevel(): Int = PLACE_MAP_ZOOM_LEVEL

                        override fun onMapReady(map: KakaoMap) {
                            kakaoMap = map
                            disableMapGestures(map)
                        }
                    },
                )
            }
        },
        update = {},
        onRelease = { releasedMapView ->
            releasedMapView.finish()
            kakaoMap = null
            mapView = null
        },
    )

    DisposableEffect(lifecycleOwner, mapView) {
        val observer =
            LifecycleEventObserver { _, event ->
                val currentMapView = mapView ?: return@LifecycleEventObserver
                when (event) {
                    Lifecycle.Event.ON_RESUME -> currentMapView.resume()
                    Lifecycle.Event.ON_PAUSE -> currentMapView.pause()
                    Lifecycle.Event.ON_DESTROY -> currentMapView.finish()
                    else -> Unit
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

private fun createPlaceMarkerStyles(
    context: Context,
    kakaoMap: KakaoMap,
): LabelStyles? =
    kakaoMap.labelManager?.addLabelStyles(
        LabelStyles.from(
            LabelStyle
                .from(createPlaceMarkerBitmap(context))
                .setAnchorPoint(0.5f, 1.0f)
                .setZoomLevel(0),
        ),
    )

private fun createPlaceMarkerBitmap(context: Context): Bitmap {
    val width = context.dpToPx(32f)
    val height = context.dpToPx(40f)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(34, 197, 94)
            style = Paint.Style.FILL
            setShadowLayer(context.dpToPx(4f).toFloat(), 0f, context.dpToPx(2f).toFloat(), Color.argb(64, 0, 0, 0))
        }
    val path =
        Path().apply {
            moveTo(width / 2f, height - 1f)
            cubicTo(
                context.dpToPx(10f).toFloat(),
                context.dpToPx(31f).toFloat(),
                context.dpToPx(4f).toFloat(),
                context.dpToPx(24f).toFloat(),
                context.dpToPx(4f).toFloat(),
                context.dpToPx(15f).toFloat(),
            )
            arcTo(
                context.dpToPx(4f).toFloat(),
                context.dpToPx(3f).toFloat(),
                context.dpToPx(28f).toFloat(),
                context.dpToPx(27f).toFloat(),
                180f,
                180f,
                false,
            )
            cubicTo(
                context.dpToPx(28f).toFloat(),
                context.dpToPx(24f).toFloat(),
                context.dpToPx(22f).toFloat(),
                context.dpToPx(31f).toFloat(),
                width / 2f,
                height - 1f,
            )
            close()
        }

    canvas.drawPath(path, paint)
    paint.clearShadowLayer()
    paint.color = Color.WHITE
    canvas.drawCircle(width / 2f, context.dpToPx(15f).toFloat(), context.dpToPx(5f).toFloat(), paint)
    return bitmap
}

private fun Context.dpToPx(dp: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()

private fun disableMapGestures(kakaoMap: KakaoMap) {
    GestureType.values().forEach { gestureType ->
        if (gestureType != GestureType.Unknown) {
            kakaoMap.setGestureEnable(gestureType, false)
        }
    }
}
