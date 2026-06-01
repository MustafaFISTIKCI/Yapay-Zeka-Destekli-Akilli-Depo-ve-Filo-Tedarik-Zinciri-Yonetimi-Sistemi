package com.logisticspro.app.ui.screens.fleet

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.logisticspro.app.data.model.RoutePoint

@Composable
fun FleetMapScreen(route: List<RoutePoint>) {
    if (route.isEmpty()) return

    val startPoint = LatLng(route.first().latitude, route.first().longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startPoint, 12f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        route.forEachIndexed { index, point ->
            Marker(
                state = MarkerState(position = LatLng(point.latitude, point.longitude)),
                title = if (index == 0) "Başlangıç" else "Teslimat Noktası ${index}",
                snippet = "${point.latitude}, ${point.longitude}"
            )
        }

        Polyline(
            points = route.map { LatLng(it.latitude, it.longitude) },
            color = Color.Blue,
            width = 8f
        )
    }
}
