package com.gigmiles.app

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private const val OPEN_MAP_STYLE = "https://tiles.openfreemap.org/styles/liberty"

@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            MapView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                onCreate(null)
                getMapAsync { map -> map.setStyle(Style.Builder().fromUri(OPEN_MAP_STYLE)) }
            }
        },
        update = { }
    )
}
