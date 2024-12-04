package com.daniellumbu.thetraveljournal.ui.screen.map
import android.Manifest
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.daniellumbu.thetraveljournal.R
import com.daniellumbu.thetraveljournal.data.MarkerDatabase
import com.daniellumbu.thetraveljournal.data.MarkerEntity
import com.daniellumbu.thetraveljournal.navigation.MainNavigation
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.Random


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapsScreen(
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Camera state and map properties
    val cameraState = remember {
        CameraPosition.fromLatLngZoom(LatLng(47.0, 19.0), 10f)
    }

    var uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = true,
                zoomGesturesEnabled = true,
                //myLocationButtonEnabled = true
            )
        )
    }

    val mapProperties = remember {
        mutableStateOf(
            MapProperties(
                mapType = MapType.NORMAL,
                isTrafficEnabled = true,
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.mymapconfig)
            )
        )
    }

    val coroutineScope = rememberCoroutineScope()
    var geocodeText by rememberSaveable { mutableStateOf("") }

    // Permission handling

    Column {
        // Permission and location updates
        val fineLocationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
        if (fineLocationPermissionState.status.isGranted) {
            Column {
                Button(onClick = { mapViewModel.startLocationMonitoring() }) {
                    Text(text = "Start location monitoring")
                }
//                Text(
//                    text = "Location: " +
//                            "${getLocationText(mapViewModel.locationState.value)}"
//                )

            }
        } else {
            Column {
                val permissionText = if (fineLocationPermissionState.status.shouldShowRationale) {
                    "Please consider giving permission"
                } else {
                    "Give permission for location"
                }
                Text(text = permissionText)
                Button(onClick = { fineLocationPermissionState.launchPermissionRequest() }) {
                    Text(text = "Request permission")
                }
            }
        }

        // Map toggle (Satellite/Normal)
        var isSatellite by remember { mutableStateOf(false) }
        Switch(
            checked = isSatellite,
            onCheckedChange = {
                isSatellite = it
                mapProperties.value = mapProperties.value.copy(
                    mapType = if (isSatellite) MapType.SATELLITE else MapType.NORMAL
                )
            }
        )
        Text(text = geocodeText)

        // Google Map with native MapView
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView ->
                val mapView = com.google.android.gms.maps.MapView(context)
                mapView.onCreate(null)
                mapView.getMapAsync { googleMap ->
                    // Set map properties
                    googleMap.mapType = mapProperties.value.mapType.ordinal
                    googleMap.isTrafficEnabled = mapProperties.value.isTrafficEnabled
                    googleMap.uiSettings.isZoomControlsEnabled = true

                    // Custom info window
                    setupMarkersWithCustomInfoWindow(context, googleMap, mapViewModel)

                    // Map long-click events
                    googleMap.setOnMapLongClickListener { latLng ->
                        // Geocode to get location name for the marker title
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val locationName = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                            ?.firstOrNull()
                            ?.countryName
                            ?: "Unknown Location"

                        val markerOptions = MarkerOptions()
                            .position(latLng)
                            .title(locationName) // Now it will show only the country name
                            .snippet("Dynamic marker with image") // Add any other relevant information
                             // Use the same image as Marker AIT

                        googleMap.addMarker(markerOptions)

                        val markerEntity = MarkerEntity(
                            latitude = markerOptions.position.latitude,
                            longitude = markerOptions.position.longitude,
                            title = markerOptions.title ?: "No Title",
                            snippet = markerOptions.snippet ?: "No Snippet",
                            imageUrl = "drawable/clothes" // Replace with your image URL or file path
                        )

                        CoroutineScope(Dispatchers.IO).launch {
                            MarkerDatabase.getDatabase(context).markerDao().insertMarker(markerEntity)
                        }

                        // Optionally, animate camera to the new marker
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                    }
                }
                mapView
            }
        )
    }
}


fun setupMarkersWithCustomInfoWindow(context: Context, googleMap: GoogleMap, mapViewModel: MapViewModel) {
    // Initialize the database
    val markerDatabase = MarkerDatabase.getDatabase(context)

    // Set custom info window adapter
    googleMap.setInfoWindowAdapter(CustomInfoWindowAdapter(context))

    // Function to save marker to the database
    fun saveMarkerToDatabase(markerOptions: MarkerOptions) {
        val markerEntity = MarkerEntity(
            latitude = markerOptions.position.latitude,
            longitude = markerOptions.position.longitude,
            title = markerOptions.title ?: "No Title",
            snippet = markerOptions.snippet ?: "No Snippet",
            imageUrl = "drawable/clothes" // Replace with a default image URL or file path
        )
        CoroutineScope(Dispatchers.IO).launch {
            markerDatabase.markerDao().insertMarker(markerEntity)
        }
    }

    // Function to load markers from the database
    fun loadMarkersFromDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            val savedMarkers = markerDatabase.markerDao().getAllMarkers()
            Log.d("YourTag", "Saved Markers: $savedMarkers")
            if (savedMarkers.isEmpty()) {
                Log.e("Money", "No markers found in the database.")
            }
            withContext(Dispatchers.Main) {
                for (markerEntity in savedMarkers) {
                    val markerOptions = MarkerOptions()
                        .position(LatLng(markerEntity.latitude, markerEntity.longitude))
                        .title(markerEntity.title)
                        .snippet(markerEntity.snippet)
                    googleMap.addMarker(markerOptions)
                }
            }
        }
    }

    // Load saved markers from the database
    loadMarkersFromDatabase()

    // Add default marker and save it to the database
    val defaultMarkerOptions = MarkerOptions()
        .position(LatLng(47.0, 19.0))
        .title("Marker AIT")
        .snippet("Marker with an image")
    val defaultMarker = googleMap.addMarker(defaultMarkerOptions)
    //saveMarkerToDatabase(defaultMarkerOptions)

    // Add markers from ViewModel and save them to the database
    mapViewModel.getMarkersList().forEach { position ->
        val dynamicMarkerOptions = MarkerOptions()
            .position(position)
            .title("Dynamic Marker")
            .snippet("Dynamic marker info")
        googleMap.addMarker(dynamicMarkerOptions)
        //saveMarkerToDatabase(dynamicMarkerOptions)
    }

    // Handle marker clicks
    googleMap.setOnMarkerClickListener { marker ->
        // Show the marker's info window
        marker.showInfoWindow()

        // Open BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(context)
        val bottomSheetView = LayoutInflater.from(context).inflate(R.layout.marker_bottom_sheet, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        // Access views in the bottom sheet
        bottomSheetView.findViewById<TextView>(R.id.marker_title).text = marker.title
        bottomSheetView.findViewById<TextView>(R.id.marker_snippet).text = marker.snippet

        val imageView = bottomSheetView.findViewById<ImageView>(R.id.marker_image)
        // Load image (example with a placeholder image)
        imageView.setImageResource(R.drawable.clothes) // Replace with your drawable

        bottomSheetView.findViewById<Button>(R.id.marker_details_button).setOnClickListener {
            MainNavigation.SummaryScreen.route
            bottomSheetDialog.dismiss()
        }

        bottomSheetView.findViewById<Button>(R.id.marker_close_button).setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetView.layoutParams?.height = (context.resources.displayMetrics.heightPixels * 0.60).toInt() // 60% of screen height

        // Dismiss the info window when the bottom sheet is dismissed
        bottomSheetDialog.setOnDismissListener {
            marker.hideInfoWindow()
        }

        // Optional: Remove background dimming
        bottomSheetDialog.window?.setDimAmount(0f)
        bottomSheetDialog.show()
        true // Return true to consume the event
    }
}




