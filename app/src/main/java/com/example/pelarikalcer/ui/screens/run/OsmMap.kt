package com.example.pelarikalcer.ui.screens.run

import android.graphics.Color as AndroidColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.CustomZoomButtonsController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

private const val STADIA_API_KEY = "8c32b51c-474d-49df-9aba-84085d88e545"

private val stadiaTileSource = XYTileSource(
    "StadiaAlidadeSmooth",
    0, 20, 256,
    "@2x.png?api_key=$STADIA_API_KEY",
    arrayOf("https://tiles.stadiamaps.com/tiles/alidade_smooth/"),
    "© Stadia Maps © OpenMapTiles © OpenStreetMap contributors"
)

@Composable
fun RunScreen() {
    var hasLocationPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasLocationPermission = granted }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    if (hasLocationPermission) {
        OsmMap(modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun OsmMap(
    modifier: Modifier = Modifier,
    centerPoint: GeoPoint = GeoPoint(-6.2088, 106.8456),
    zoomLevel: Double = 16.5,
    showUserLocation: Boolean = true,
    onMapReady: (MapView) -> Unit = {}
) {
    val context = LocalContext.current

    // Set dark background color and clip to avoid white tile flickering during horizontal pager swipe
    val (mapView, locationOverlay) = remember {
        val mv = MapView(context).apply {
            setBackgroundColor(AndroidColor.parseColor("#0A0E1A")) // Match DeepNavy app background
            setTileSource(stadiaTileSource)
            setMultiTouchControls(true)
            controller.setZoom(zoomLevel)
            controller.setCenter(centerPoint)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        }
        val overlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mv).apply {
            enableMyLocation()
            enableFollowLocation()
        }
        mv.overlays.add(overlay)
        onMapReady(mv)
        Pair(mv, overlay)
    }

    DisposableEffect(Unit) {
        if (showUserLocation) locationOverlay.enableMyLocation()
        onDispose {
            locationOverlay.disableMyLocation()
            mapView.onDetach()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { }
    )
}
