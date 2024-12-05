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
import androidx.navigation.NavController
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
import com.bumptech.glide.Glide


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapsScreen(
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel = hiltViewModel(),
    navController: NavController,
    markerViewModel: MarkerViewModel = hiltViewModel()
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
                    setupMarkersWithCustomInfoWindow(context, googleMap, mapViewModel,navController,markerViewModel )

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
                            imageUrls = listOf("android.resource://${context.packageName}/drawable/clothes") // Replace with your image URLs or file paths
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


fun setupMarkersWithCustomInfoWindow(
    context: Context,
    googleMap: GoogleMap,
    mapViewModel: MapViewModel,
    navController: NavController,
    markerViewModel: MarkerViewModel
) {
    // Initialize the database
    val markerDatabase = MarkerDatabase.getDatabase(context)

    // Load saved markers from the database
    fun loadMarkersFromDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            val savedMarkers = markerDatabase.markerDao().getAllMarkers()
            Log.d("YourTag", "Saved Markers: $savedMarkers")
            if (savedMarkers.isEmpty()) {
                Log.e("MarkerError", "No markers found in the database.")
            }
            withContext(Dispatchers.Main) {
                for (markerEntity in savedMarkers) {
                    val markerOptions = MarkerOptions()
                        .position(LatLng(markerEntity.latitude, markerEntity.longitude))
                        .title(markerEntity.title)
                        .snippet(markerEntity.snippet)
                    val marker = googleMap.addMarker(markerOptions)

                    // Attach imageUrls to the marker's tag for later use
                    marker?.tag = markerEntity
                }
            }
        }
    }

    // Load markers initially
    loadMarkersFromDatabase()

    googleMap.setOnMarkerClickListener { marker ->
        marker.showInfoWindow()

        // Safely cast the tag to the expected type
        val markerEntity = marker.tag as? MarkerEntity
        if (markerEntity == null) {
            Log.e("MarkerError", "Marker tag is null or not of the expected type.")
            return@setOnMarkerClickListener true
        }

        val bottomSheetDialog = BottomSheetDialog(context)
        val bottomSheetView = LayoutInflater.from(context).inflate(R.layout.marker_bottom_sheet, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        // Populate bottom sheet views with marker details
        bottomSheetView.findViewById<TextView>(R.id.marker_title).text = markerEntity.title
        bottomSheetView.findViewById<TextView>(R.id.marker_snippet).text = markerEntity.snippet

        val imageView = bottomSheetView.findViewById<ImageView>(R.id.marker_image)
        if (markerEntity.imageUrls.isNotEmpty()) {
            Glide.with(context)
                .load(markerEntity.imageUrls[0])
                .placeholder(R.drawable.img)
                .error(R.drawable.bracket)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.bracket)
        }

        // Set the "Details" button logic
        bottomSheetView.findViewById<Button>(R.id.marker_details_button).setOnClickListener {
            // Update the selected marker in ViewModel
            markerViewModel.selectMarker(markerEntity)

            // Navigate to the details screen, passing the marker ID
            navController.navigate("DetailScreenContent/${markerEntity.id}")
            bottomSheetDialog.dismiss()
        }

        bottomSheetView.findViewById<Button>(R.id.marker_close_button).setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
        true
    }
}





