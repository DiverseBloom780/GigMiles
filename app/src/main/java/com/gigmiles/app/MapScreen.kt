package com.gigmiles.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private const val OPEN_MAP_STYLE = "https://tiles.openfreemap.org/styles/liberty"

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
            onStart()
            getMapAsync { map -> map.setStyle(Style.Builder().fromUri(OPEN_MAP_STYLE)) }
        }
    }
    DisposableEffect(mapView) {
        onDispose {
            mapView.onStop()
            mapView.onDestroy()
        }
    }
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { mapView },
        update = { }
    )
}
