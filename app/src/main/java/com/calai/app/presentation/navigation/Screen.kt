package com.calai.app.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object AddMeal : Screen("add_meal")
    object CameraScan : Screen("camera_scan")
    object Statistics : Screen("statistics")
    object Chat : Screen("chat")
    object Profile : Screen("profile")
    object Suggestions : Screen("suggestions")
}
