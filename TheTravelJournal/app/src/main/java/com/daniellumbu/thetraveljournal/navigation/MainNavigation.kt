package com.daniellumbu.thetraveljournal.navigation


sealed class MainNavigation(val route: String) {
    object SplashScreen: MainNavigation("splash_screen")

    object MapsScreen: MainNavigation("maps_screen")

    object TodoListScreen: MainNavigation("TodoList_screen")

    object DetailsScreenContent : MainNavigation("DetailScreenContent/{markerId}") {
        fun createRoute(markerId: Int) = "DetailScreenContent/$markerId"
    }

}