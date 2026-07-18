package com.yagubogu.ui.common.component

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
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
import com.yagubogu.BuildKonfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

private val fallbackPlacePosition = LatLng.from(37.512150, 127.071960)
private const val PLACE_MAP_ZOOM_LEVEL = 3

@Composable
actual fun PlaceMapView(
    address: String,
    placeName: String,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var position by remember(address) { mutableStateOf<LatLng?>(null) }
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    LaunchedEffect(address) {
        position = geocodeAddress(context, address) ?: fallbackPlacePosition
    }

    LaunchedEffect(position, kakaoMap) {
        val target = position ?: return@LaunchedEffect
        kakaoMap?.moveCamera(CameraUpdateFactory.newCenterPosition(target, PLACE_MAP_ZOOM_LEVEL))
        kakaoMap?.labelManager?.layer?.apply {
            removeAll()
            addLabel(LabelOptions.from(target))
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
                        override fun getPosition(): LatLng = position ?: fallbackPlacePosition

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

private fun disableMapGestures(kakaoMap: KakaoMap) {
    GestureType.values().forEach { gestureType ->
        if (gestureType != GestureType.Unknown) {
            kakaoMap.setGestureEnable(gestureType, false)
        }
    }
}

@Suppress("DEPRECATION")
@SuppressLint("MissingPermission")
private suspend fun geocodeAddress(
    context: Context,
    address: String,
): LatLng? =
    withContext(Dispatchers.IO) {
        runCatching {
            Geocoder(context, Locale.KOREA)
                .getFromLocationName(address, 1)
                ?.firstOrNull()
                ?.let { location -> LatLng.from(location.latitude, location.longitude) }
        }.getOrNull()
    }
