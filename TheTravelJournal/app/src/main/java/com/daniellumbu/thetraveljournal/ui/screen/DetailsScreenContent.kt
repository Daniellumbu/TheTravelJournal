package com.daniellumbu.thetraveljournal.ui.screen
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.daniellumbu.thetraveljournal.ui.screen.map.MarkerViewModel
import com.google.accompanist.pager.*


class DetailsScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtain ViewModel instance
        val markerViewModel: MarkerViewModel by viewModels()

        setContent {
            DetailsScreenContent(markerViewModel = markerViewModel)
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun DetailsScreenContent(
    markerViewModel: MarkerViewModel // Pass the ViewModel as a parameter
) {
    // Observe the selected marker from the ViewModel
    val selectedMarker by markerViewModel.selectedMarker.collectAsState()

    // State to manage photos and actions
    var photos by remember { mutableStateOf(listOf<Uri>()) }
    var showFullScreen by remember { mutableStateOf(false) }
    var currentPhotoIndex by remember { mutableStateOf(0) }

    // Handle image picker
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { newUri ->
                val updatedMarker = selectedMarker?.copy(
                    imageUrls = selectedMarker!!.imageUrls + newUri.toString()
                )
                updatedMarker?.let {
                    markerViewModel.updateMarker(it) // Save updated marker through ViewModel
                    photos = updatedMarker.imageUrls.map { Uri.parse(it) }
                }
            }
        }
    )

    // Load the photos when selected marker changes
    LaunchedEffect(selectedMarker) {
        photos = selectedMarker?.imageUrls?.map { Uri.parse(it) } ?: emptyList()
    }

    if (showFullScreen) {
        // Fullscreen Photo Viewer
        FullScreenPhotoViewer(
            photos = photos,
            initialPage = currentPhotoIndex,
            onDismiss = { showFullScreen = false }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = selectedMarker?.title ?: "Loading...",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = selectedMarker?.snippet ?: "",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Justify,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(
                onClick = { pickImageLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Add Photo")
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(photos.size) { index ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                currentPhotoIndex = index
                                showFullScreen = true
                            }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(photos[index]),
                            contentDescription = "Photo $index",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalPagerApi::class)
@Composable
fun FullScreenPhotoViewer(
    photos: List<Uri>,
    initialPage: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage)

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            count = photos.size,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Image(
                painter = rememberAsyncImagePainter(photos[page]),
                contentDescription = "Full View Photo $page",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
        }
    }
}
