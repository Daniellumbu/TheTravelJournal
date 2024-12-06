package com.daniellumbu.thetraveljournal.ui.screen
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.daniellumbu.thetraveljournal.ui.screen.map.MarkerViewModel
import com.google.accompanist.pager.*
import java.io.File


class DetailsScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val markerViewModel: MarkerViewModel by viewModels()

        // Get marker ID from the intent
        val markerId = intent?.getIntExtra("MARKER_ID", -1) ?: -1
        if (markerId != -1) {
            markerViewModel.loadMarker(markerId) // Load the marker data from the ViewModel
        }

        setContent {
            DetailsScreenContent(markerViewModel = markerViewModel, markerId)
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun DetailsScreenContent(
    markerViewModel: MarkerViewModel,
    markerId: Int
) {
    val context = LocalContext.current
    LaunchedEffect(markerId) {
        markerViewModel.loadMarker(markerId)
    }

    val selectedMarker by markerViewModel.selectedMarker.collectAsState()

    var photos by remember { mutableStateOf(listOf<Uri>()) }
    var showFullScreen by remember { mutableStateOf(false) }
    var currentPhotoIndex by remember { mutableStateOf(0) }

    // Handle image picker
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { newUri ->
                val savedFileUri = saveUriToInternalStorage(context, newUri)

                savedFileUri?.let { fileUri ->
                    val updatedMarker = selectedMarker?.copy(
                        imageUrls = selectedMarker!!.imageUrls + fileUri.toString()
                    )
                    updatedMarker?.let {
                        markerViewModel.updateMarker(it)
                        photos = updatedMarker.imageUrls.map { Uri.parse(it) }
                    }
                }
            }
        }
    )

    // Load the photos when selected marker changes
    LaunchedEffect(selectedMarker) {
        photos = selectedMarker?.imageUrls?.mapNotNull { imageUrl ->
            val file = File(Uri.parse(imageUrl).path ?: "")
            if (file.exists()) Uri.fromFile(file) else null
        } ?: emptyList()
    }

    if (showFullScreen) {
        FullScreenPhotoViewer(
            photos = photos,
            initialPage = currentPhotoIndex,
            onDismiss = { showFullScreen = false },
            onDelete = { uri ->
                markerViewModel.deleteImageFromMarker(uri) // Handle image deletion
                photos = photos.filterNot { it == uri } // Remove photo from list
            }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter // Align the content at the top-center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter) // Make sure the column is centered
            ) {
                // Add Spacer to adjust vertical position
                Spacer(modifier = Modifier.height(50.dp)) // Adjust as needed to push content lower

                Text(
                    text = selectedMarker?.title ?: "Loading...",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center, // Center the title
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                Text(
                    text = selectedMarker?.snippet ?: "",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center, // Center the snippet
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
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
}


fun saveUriToInternalStorage(context: Context, uri: Uri): Uri? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = "${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)

        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


@OptIn(ExperimentalPagerApi::class)
@Composable
fun FullScreenPhotoViewer(
    photos: List<Uri>,
    initialPage: Int,
    onDismiss: () -> Unit,
    onDelete: (Uri) -> Unit // Add delete callback
) {
    val pagerState = rememberPagerState(initialPage)
    Log.d("Marker Tag", "The initial int: $initialPage")

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

        // Delete button - Adjusted closer to top
        IconButton(
            onClick = {
                val photoToDelete = photos[pagerState.currentPage]
                onDelete(photoToDelete) // Call delete action
                onDismiss() // Close the full-screen viewer after deletion
            },
            modifier = Modifier
                .align(Alignment.TopEnd) // Align the button closer to the top-right corner
                .padding(35.dp) // Increase padding to move it a bit lower
                .size(90.dp) // Make the button larger
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Photo",
                tint = Color.Red// Set the color to blue
            )
        }

        // Close button - Adjusted closer to top
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopStart) // Align the button closer to the top-left corner
                .padding(35.dp) // Increase padding for more spacing
                .size(90.dp) // Make the button larger
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.Blue // Set the color to blue
            )
        }
    }
}

