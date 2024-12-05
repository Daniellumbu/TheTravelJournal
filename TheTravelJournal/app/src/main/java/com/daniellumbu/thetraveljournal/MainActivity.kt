package com.daniellumbu.thetraveljournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import com.daniellumbu.thetraveljournal.navigation.MainNavigation
import com.daniellumbu.thetraveljournal.ui.screen.DetailsScreenContent
import com.daniellumbu.thetraveljournal.ui.screen.TodoListScreen
import com.daniellumbu.thetraveljournal.ui.theme.TheTravelJournalTheme
import kotlinx.coroutines.delay
import com.daniellumbu.thetraveljournal.ui.screen.map.MapsScreen
import com.daniellumbu.thetraveljournal.ui.screen.map.MarkerViewModel


@AndroidEntryPoint
class MainActivity() : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TheTravelJournalTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TodoAppNavHost(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun TodoAppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = MainNavigation.SplashScreen.route // Updated to SplashScreen
) {
    NavHost(
        navController = navController, startDestination = startDestination
    ) {
        // Splash Screen
        composable(MainNavigation.SplashScreen.route) {
            SplashScreen {
                navController.navigate(MainNavigation.MapsScreen.route) {
                    // Remove SplashScreen from the back stack
                    popUpTo(MainNavigation.SplashScreen.route) { inclusive = true }
                }
            }
        }

        // Maps Screen
        composable(MainNavigation.MapsScreen.route) {
            MapsScreen(navController = navController)
        }

        // Summary Screen
        composable(MainNavigation.DetailsScreenContent.route) { backStackEntry ->
            val markerId = backStackEntry.arguments?.getString("markerId")?.toIntOrNull() ?: -1

            if (markerId != -1) {
                val markerViewModel: MarkerViewModel = hiltViewModel() // Get the ViewModel
                // Pass the markerId and ViewModel to DetailsScreenContent
                DetailsScreenContent(markerViewModel = markerViewModel, markerId = markerId)
            }
        }
    }
}


@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // Use LaunchedEffect to perform a side-effect on composition
    LaunchedEffect(Unit) {
        delay(3000L) // 3-second delay
        onTimeout()
    }

    // Your custom logo or image
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo), // Replace with your image resource
            contentDescription = "App Logo"
        )
    }
}
