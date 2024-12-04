package com.daniellumbu.thetraveljournal.navigation


sealed class MainNavigation(val route: String) {
    object SplashScreen: MainNavigation("splash_screen")

    object MapsScreen: MainNavigation("maps_screen")


    object SummaryScreen : MainNavigation(
        "summaryscreen?all={all}&important={important}") {
        fun createRoute(all: Int, important: Int) : String {
            return "summaryscreen?all=$all&important=$important"
        }
    }

}